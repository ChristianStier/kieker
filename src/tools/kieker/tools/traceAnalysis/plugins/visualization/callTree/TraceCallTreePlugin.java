/***************************************************************************
 * Copyright 2011 by
 *  + Christian-Albrechts-University of Kiel
 *    + Department of Computer Science
 *      + Software Engineering Group 
 *  and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ***************************************************************************/

package kieker.tools.traceAnalysis.plugins.visualization.callTree;

import kieker.analysis.plugin.configuration.IInputPort;
import kieker.tools.traceAnalysis.plugins.traceReconstruction.TraceProcessingException;
import kieker.tools.traceAnalysis.plugins.AbstractMessageTraceProcessingPlugin;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Hashtable;
import java.util.Stack;
import java.util.Vector;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import kieker.tools.traceAnalysis.systemModel.AllocationComponent;
import kieker.tools.traceAnalysis.systemModel.Message;
import kieker.tools.traceAnalysis.systemModel.MessageTrace;
import kieker.tools.traceAnalysis.systemModel.Operation;
import kieker.tools.traceAnalysis.systemModel.Signature;
import kieker.tools.traceAnalysis.systemModel.SynchronousCallMessage;
import kieker.tools.traceAnalysis.systemModel.SynchronousReplyMessage;
import kieker.tools.traceAnalysis.systemModel.repository.AbstractSystemSubRepository;
import kieker.tools.traceAnalysis.systemModel.repository.AllocationComponentOperationPairFactory;
import kieker.tools.traceAnalysis.systemModel.repository.SystemModelRepository;
import kieker.analysis.plugin.configuration.AbstractInputPort;
import kieker.tools.traceAnalysis.plugins.visualization.util.IntContainer;
import kieker.tools.traceAnalysis.plugins.visualization.util.dot.DotFactory;

/**
 * Plugin providing the creation of calling trees both for individual traces 
 * and an aggregated form mulitple traces.
 *
 * @author Andre van Hoorn
 */
public class TraceCallTreePlugin extends AbstractMessageTraceProcessingPlugin {

    private static final Log log = LogFactory.getLog(TraceCallTreePlugin.class);
    private final CallTreeNode root;
    private final AllocationComponentOperationPairFactory allocationComponentOperationPairFactory;
    private final SystemModelRepository systemEntityFactory;
    private final String outputFnBase;
    private final boolean shortLabels;

    public TraceCallTreePlugin(
            final String name,
            final AllocationComponentOperationPairFactory allocationComponentOperationPairFactory,
            final SystemModelRepository systemEntityFactory,
            final String outputFnBase, final boolean shortLabels) {
        super(name, systemEntityFactory);
        this.allocationComponentOperationPairFactory = allocationComponentOperationPairFactory;
        this.systemEntityFactory = systemEntityFactory;
        root = new CallTreeNode(null, // null: root node has no parent
                new CallTreeOperationHashKey(this.systemEntityFactory.getAllocationFactory().rootAllocationComponent,
                this.systemEntityFactory.getOperationFactory().rootOperation));
        this.outputFnBase = outputFnBase;
        this.shortLabels = shortLabels;
    }

    private static final String nodeLabel(final CallTreeNode node, final boolean shortLabels) {
        if (node.isRootNode()) {
            return "$";
        }

        AllocationComponent component = node.getAllocationComponent();
        Operation operation = node.getOperation();
        String resourceContainerName = component.getExecutionContainer().getName();
        String assemblyComponentName = component.getAssemblyComponent().getName();
        String componentTypePackagePrefx = component.getAssemblyComponent().getType().getPackageName();
        String componentTypeIdentifier = component.getAssemblyComponent().getType().getTypeName();

        StringBuilder strBuild = new StringBuilder(resourceContainerName).append("::\\n").append(assemblyComponentName).append(":");
        if (!shortLabels) {
            strBuild.append(componentTypePackagePrefx);
        } else {
            strBuild.append("..");
        }
        strBuild.append(componentTypeIdentifier).append(".");

        Signature sig = operation.getSignature();
        StringBuilder opLabel = new StringBuilder(sig.getName());
        opLabel.append("(");
        String[] paramList = sig.getParamTypeList();
        if (paramList != null && paramList.length > 0) {
            opLabel.append("..");
        }
        opLabel.append(")");

        strBuild.append(opLabel.toString());
        return strBuild.toString();
    }

    /** Traverse tree recursively and generate dot code for edges. */
    private static void dotEdgesFromSubTree(final SystemModelRepository systemEntityFactory,
            CallTreeNode n,
            Hashtable<CallTreeNode, Integer> nodeIds,
            IntContainer nextNodeId, PrintStream ps, final boolean shortLabels) {
        StringBuilder strBuild = new StringBuilder();
        nodeIds.put(n, nextNodeId.i);
        strBuild.append(nextNodeId.i++).append("[label =\"").append(nodeLabel(n, shortLabels)).append("\",shape=" + DotFactory.DOT_SHAPE_NONE
                + ",style=" + DotFactory.DOT_STYLE_FILLED
                + ",fillcolor=" + DotFactory.DOT_FILLCOLOR_WHITE + "];");
        ps.println(strBuild.toString());
        for (CallTreeNode child : n.getChildren()) {
            dotEdgesFromSubTree(systemEntityFactory, child, nodeIds, nextNodeId, ps, shortLabels);
        }
    }

    /** Traverse tree recursively and generate dot code for vertices. */
    private static void dotVerticesFromSubTree(final CallTreeNode n,
            final Hashtable<CallTreeNode, Integer> nodeIds,
            final PrintStream ps, final boolean includeWeights) {
        int thisId = nodeIds.get(n);
        for (CallTreeNode child : n.getChildren()) {
            StringBuilder strBuild = new StringBuilder();
            int childId = nodeIds.get(child);
            strBuild.append("\n").append(thisId).append("->").append(childId).append("[style=solid,arrowhead=none");
            if (includeWeights) {
                strBuild.append(",label = ").append("").append(", weight =").append("");
            }
            strBuild.append(" ]");
            ps.println(strBuild.toString());
            dotVerticesFromSubTree(child, nodeIds, ps, includeWeights);
        }
    }

    private static void dotFromCallingTree(final SystemModelRepository systemEntityFactory,
            final CallTreeNode root, final PrintStream ps,
            final boolean includeWeights, final boolean shortLabels) {
        // preamble:
        ps.println("digraph G {");
        StringBuilder edgestringBuilder = new StringBuilder();

        Hashtable<CallTreeNode, Integer> nodeIds = new Hashtable<CallTreeNode, Integer>();

        dotEdgesFromSubTree(systemEntityFactory, root, nodeIds, new IntContainer(0), ps, shortLabels);
        dotVerticesFromSubTree(root, nodeIds, ps, includeWeights);

        ps.println(edgestringBuilder.toString());
        ps.println("}");
    }

    private static void saveTreeToDotFile(final SystemModelRepository systemEntityFactory,
            final CallTreeNode root, final String outputFnBase, final boolean includeWeights,
            final boolean shortLabels) throws FileNotFoundException {
        PrintStream ps = new PrintStream(new FileOutputStream(outputFnBase + ".dot"));
        dotFromCallingTree(systemEntityFactory, root, ps, includeWeights, shortLabels);
        ps.flush();
        ps.close();
    }

    public void saveTreeToDotFile(final String outputFnBase, final boolean includeWeights,
            final boolean shortLabels) throws FileNotFoundException {
        saveTreeToDotFile(this.systemEntityFactory, this.root, outputFnBase, includeWeights, shortLabels);
        this.printMessage(new String[]{
                    "Wrote call tree to file '" + outputFnBase + ".dot" + "'",
                    "Dot file can be converted using the dot tool",
                    "Example: dot -T svg " + outputFnBase + ".dot" + " > " + outputFnBase + ".svg"
                });
    }

    private static void addTraceToTree(final CallTreeNode root,
            final MessageTrace t, final boolean aggregated)
            throws TraceProcessingException {
        Stack<CallTreeNode> curStack = new Stack<CallTreeNode>();

        Vector<Message> msgTraceVec = t.getSequenceAsVector();
        CallTreeNode curNode = root;
        curStack.push(curNode);
        for (final Message m : msgTraceVec) {
            if (m instanceof SynchronousCallMessage) {
                curNode = curStack.peek();
                CallTreeNode child;
                if (aggregated) {
                    child = curNode.getChild(m.getReceivingExecution().getAllocationComponent(),
                            m.getReceivingExecution().getOperation());
                } else {
                    child = curNode.createNewChild(m.getReceivingExecution().getAllocationComponent(),
                            m.getReceivingExecution().getOperation());
                }
                curNode = child;
                curStack.push(curNode);
            } else if (m instanceof SynchronousReplyMessage) {
                curNode = curStack.pop();
            } else {
                throw new TraceProcessingException("Message type not supported:" + m.getClass().getName());
            }
        }
        if (curStack.pop() != root) {
            log.fatal("Stack not empty after processing trace");
            throw new TraceProcessingException("Stack not empty after processing trace");
        }
    }

    public static void writeDotForMessageTrace(final SystemModelRepository systemEntityFactory,
            final MessageTrace msgTrace, final String outputFilename, final boolean includeWeights,
            final boolean shortLabels) throws FileNotFoundException, TraceProcessingException {
        final CallTreeNode root = new CallTreeNode(null,
                new CallTreeOperationHashKey(systemEntityFactory.getAllocationFactory().rootAllocationComponent,
                systemEntityFactory.getOperationFactory().rootOperation));
        addTraceToTree(root, msgTrace, false); // false: no aggregation
        saveTreeToDotFile(systemEntityFactory, root, outputFilename, includeWeights, shortLabels);
    }

    @Override
    public void printStatusMessage() {
        super.printStatusMessage();
        final int numPlots = this.getSuccessCount();
        final long lastSuccessTracesId = this.getLastTraceIdSuccess();
        System.out.println("Wrote " + numPlots + " call tree"
                + (numPlots > 1 ? "s" : "") + " to file"
                + (numPlots > 1 ? "s" : "") + " with name pattern '"
                + outputFnBase + "-<traceId>.dot'");
        System.out.println("Dot files can be converted using the dot tool");
        System.out.println("Example: dot -T svg " + outputFnBase + "-"
                + ((numPlots > 0) ? lastSuccessTracesId : "<traceId>")
                + ".dot > " + outputFnBase + "-"
                + ((numPlots > 0) ? lastSuccessTracesId : "<traceId>")
                + ".svg");
    }

    @Override
    public boolean execute() {
        return true; // no need to do anything here
    }

    @Override
    public void terminate(final boolean error) {
        // no need to do anything here
    }

    @Override
    public IInputPort<MessageTrace> getMessageTraceInputPort() {
        return this.messageTraceInputPort;
    }
    private final IInputPort<MessageTrace> messageTraceInputPort =
            new AbstractInputPort<MessageTrace>("Message traces") {

                @Override
                public void newEvent(MessageTrace mt) {
                    try {
                        final TraceCallTreeNode rootNode = new TraceCallTreeNode(
                                AbstractSystemSubRepository.ROOT_ELEMENT_ID,
                                systemEntityFactory,
                                allocationComponentOperationPairFactory,
                                allocationComponentOperationPairFactory.rootPair,
                                true); // rootNode
                        AbstractCallTreePlugin.writeDotForMessageTrace(
                                systemEntityFactory, rootNode, mt,
                                outputFnBase + "-" + mt.getTraceId(), false,
                                shortLabels); // no weights
                        reportSuccess(mt.getTraceId());
                    } catch (final TraceProcessingException ex) {
                        reportError(mt.getTraceId());
                        log.error("TraceProcessingException", ex);
                    } catch (final FileNotFoundException ex) {
                        reportError(mt.getTraceId());
                        log.error("File not found", ex);
                    }
                }
            };
}
