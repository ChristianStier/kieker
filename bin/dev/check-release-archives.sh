#!/bin/bash

BASE_TMP_DIR="$(dirname $0)/../../tmp/"

function change_dir {
	echo "Changing dir to $1 ..."
	if ! cd ${1}; then
		echo "Failed to cd to '${1}'"
		exit 1
	fi
	echo "Current directory: $(pwd)"
}

# create tmp subdir in the current directory and change to it
function create_subdir_n_cd {
	TMPDIR=$(mktemp -d --tmpdir="$(pwd)")
	echo "Created temp dir '${TMPDIR}'"
	change_dir "${TMPDIR}"
}

# build with ant (target may be passed as $1)
function run_ant {
	echo "Trying to invoke ant with target '$1' ..."
	if ! which ant; then
		echo "Ant not found in path"
		exit 1
	fi
	if ! ant $1; then
		echo "Ant build failed"
		exit 1
	fi
}

# provide content list of archive
function print_archive_contents {
	if [ -z "$1" ]; then
		echo "No archive provided"
		exit 1
	fi
	
	if echo "$1" | grep "zip"; then
		unzip -l "$1" | awk '{ print $4 }'
		return
	fi
	
	if echo "$1" | grep "tar.gz"; then
		tar -tzf "$1"
		return
	fi
}

# extract archiv and change into directory
function extract_archive_n_cd {
	if [ -z "$1" ]; then
		echo "No archive provided"
		exit 1
	fi
	
	if echo "$1" | grep "zip"; then
		unzip -q "$1" 
	elif echo "$1" | grep "tar.gz"; then
		tar -xzf "$1"
	else
		echo "Archive '$1' is neither zip nor .tar.gz"
		exit 1
	fi 

	change_dir kieker-*
}

# lists the files included in an archive without extracting it
function cat_archive_content {
	if [ -z "$1" ]; then
		echo "No archive provided"
		exit 1
	fi
	
	if echo "$1" | grep "zip"; then
		unzip -l "$1" | awk '{ print $4 }' |grep -v "^$"
	elif echo "$1" | grep "tar.gz"; then
		tar -tzf "$1" |grep -v "^$"
	else
		echo "Archive '$1' is neither zip nor .tar.gz"
		exit 1
	fi 
}

function assert_file_NOT_exists {
	echo -n "Asserting file '$1' does not exist ..."
	if test -e "$1"; then
		echo "File '$1' should not exist"
		exit 1
	fi
	echo OK
}

function assert_dir_NOT_exists {
	echo -n "Asserting '$1' is a directory ..."
	if test -d "$1"; then
		echo "Directory '$1' should not exist"
		exit 1
	fi
	echo OK
}

function assert_dir_exists {
	echo -n "Asserting '$1' is a directory ..."
	if ! test -d "$1"; then
		echo "File '$1' is missing or not a directory"
		exit 1
	fi
	echo OK
}

function assert_file_exists_regular {
	echo -n "Asserting '$1' is a regular file ..."
	if ! test -s "$1"; then
		echo "File '$1' is missing or not a regular file"
		exit 1
	fi
	echo OK
}

# Asserts the existence of files common to the src and bin releases
function assert_files_exist_common {
	assert_dir_exists "bin/"
	assert_file_exists_regular "bin/logging.properties"
	assert_dir_exists "doc/"
	assert_dir_exists "examples/"
	assert_dir_exists "lib/"
	assert_dir_exists "lib/framework-libs/"
	assert_file_exists_regular "lib/sigar-native-libs/libsigar-x86-linux.so"
	assert_file_exists_regular "lib/sigar-native-libs/libsigar-amd64-linux.so"
	assert_file_exists_regular "lib/sigar-native-libs/sigar-amd64-winnt.dll"
	assert_file_exists_regular "lib/sigar-native-libs/sigar-x86-winnt.dll"
	assert_file_exists_regular "lib/sigar-native-libs/sigar-x86-winnt.lib"
	assert_file_exists_regular "README"
	assert_file_exists_regular "HISTORY"
	assert_file_exists_regular "LICENSE"
	assert_file_NOT_exists "build/"
	assert_file_NOT_exists "build-eclipse/"
	assert_file_NOT_exists "tmp/"
	assert_file_NOT_exists ".git/"
	assert_file_NOT_exists ".gitignore/"
	
	# check if LICENSE file for each jar
	for jar in $(find lib/ -name "*.jar"); do
		JAR_BASE=$(echo ${jar} | sed 's/\(.*\)\..*/\1/') # remove file extension
		assert_file_exists_regular "${JAR_BASE}.LICENSE"
	done
	
	# make sure that specified AspectJ version matches the present files
	assert_file_exists_regular "lib/aspectjrt-${aspectjversion}.jar"
	assert_file_exists_regular "lib/aspectjweaver-${aspectjversion}.jar"
	
	assert_file_exists_regular "examples/OverheadEvaluationMicrobenchmark/.classpath"
	assert_file_exists_regular "examples/userguide/appendix-JMS/.classpath"
	assert_file_exists_regular "examples/userguide/ch2--manual-instrumentation/.classpath"
	assert_file_exists_regular "examples/userguide/ch2--bookstore-application/.classpath"
	assert_file_exists_regular "examples/userguide/appendix-Sigar/.classpath"
	assert_file_exists_regular "examples/userguide/ch5--trace-monitoring-aspectj/.classpath"
	assert_file_exists_regular "examples/userguide/ch3-4--custom-components/.classpath"
}

# Asserts the existence of files in the src release
function assert_files_exist_src {
	assert_files_exist_common
	assert_dir_exists "model/"
	assert_file_exists_regular "model/AnalysisMetaModel.ecore"
	assert_file_exists_regular "model/AnalysisMetaModel.ecorediag"
	assert_file_exists_regular "model/AnalysisMetaModel.genmodel"
	assert_dir_exists "lib/static-analysis/"
	assert_dir_exists "src/"
	assert_dir_exists "src-gen/"
	assert_dir_exists "test/"
	assert_file_exists_regular "src/monitoring/META-INF/kieker.monitoring.default.properties"
	assert_file_NOT_exists "dist/"
	assert_file_NOT_exists "META-INF/"
	
	assert_file_NOT_exists "examples/OverheadEvaluationMicrobenchmark/lib/*.jar"
	assert_file_NOT_exists "examples/OverheadEvaluationMicrobenchmark/lib/*.jar"
	assert_file_NOT_exists "examples/userguide/ch2--manual-instrumentation/lib/*.jar"
	assert_file_NOT_exists "examples/userguide/ch3-4--custom-components/lib/*.jar"
	assert_file_NOT_exists "examples/userguide/ch5--trace-monitoring-aspectj/lib/*.jar"
	assert_file_NOT_exists "examples/userguide/appendix-JMS/lib/*.jar"
	assert_file_NOT_exists "examples/userguide/appendix-Sigar/lib/*.jar"
	
	assert_file_NOT_exists "examples/JavaEEServletContainerExample/jetty-hightide-jpetstore/webapps/jpetstore/WEB-INF/classes/META-INF/kieker.monitoring.properties"
	assert_file_NOT_exists "examples/JavaEEServletContainerExample/jetty-hightide-jpetstore/webapps/jpetstore/WEB-INF/lib/aspectjweaver-*"
	assert_file_NOT_exists "examples/JavaEEServletContainerExample/jetty-hightide-jpetstore/webapps/jpetstore/WEB-INF/lib/kieker-*.jar"
	assert_file_exists_regular "build.xml"
	assert_file_exists_regular "build.properties"
	assert_file_exists_regular "kieker-eclipse.importorder"
	assert_file_exists_regular "kieker-eclipse-cleanup.xml"
	assert_file_exists_regular "kieker-eclipse-formatter.xml"
	assert_file_exists_regular ".project"
	assert_file_exists_regular ".classpath"
	assert_file_exists_regular ".checkstyle"
	assert_file_exists_regular ".pmd"
	assert_dir_exists ".settings/"
	assert_file_exists_regular "doc/README-bin"
	assert_file_exists_regular "doc/README-src"
}

# Asserts the existence of files in the bin release
function assert_files_exist_bin {
	assert_files_exist_common
	assert_file_exists_regular "doc/kieker-"*"_userguide.pdf"
	assert_dir_exists "dist/"
	MAIN_JAR=$(ls "dist/kieker-"*".jar" | grep -v emf | grep -v aspectj )
	assert_file_exists_regular "META-INF/kieker.monitoring.properties"
	assert_file_exists_regular "META-INF/kieker.monitoring.adaptiveMonitoring.conf"
	assert_file_exists_regular ${MAIN_JAR}
	assert_file_exists_regular "dist/kieker-"*"_aspectj.jar"
	assert_file_exists_regular "dist/kieker-"*"_emf.jar"
	
	assert_file_exists_regular "examples/OverheadEvaluationMicrobenchmark/lib/kieker-"*"_aspectj.jar"
	assert_file_exists_regular "examples/OverheadEvaluationMicrobenchmark/lib/commons-cli-"*".jar"
    assert_file_exists_regular "examples/userguide/ch2--manual-instrumentation/lib/kieker-"*"_emf.jar"
    assert_file_exists_regular "examples/userguide/ch3-4--custom-components/lib/kieker-"*"_emf.jar"
    assert_file_exists_regular "examples/userguide/ch5--trace-monitoring-aspectj/lib/kieker-"*"_aspectj.jar"
    assert_file_exists_regular "examples/userguide/appendix-JMS/lib/kieker-"*".jar"
    assert_file_exists_regular "examples/userguide/appendix-Sigar/lib/kieker-"*"_emf.jar"
    assert_file_exists_regular "examples/userguide/appendix-Sigar/lib/sigar-"*".jar"
    assert_file_exists_regular "examples/userguide/appendix-Sigar/lib/libsigar-"*".so"
    assert_file_exists_regular "examples/userguide/appendix-Sigar/lib/sigar-"*".dll"
    assert_file_exists_regular "examples/userguide/appendix-Sigar/lib/sigar-"*".lib"
	
	assert_file_exists_regular "examples/JavaEEServletContainerExample/jetty-hightide-jpetstore/kieker.monitoring.properties"
	assert_file_exists_regular "examples/JavaEEServletContainerExample/jetty-hightide-jpetstore/webapps/jpetstore/WEB-INF/lib/aspectjweaver-"*
	assert_file_exists_regular "examples/JavaEEServletContainerExample/jetty-hightide-jpetstore/webapps/jpetstore/WEB-INF/lib/kieker-"*".jar"
	assert_file_NOT_exists "lib/static-analysis/"
	assert_file_NOT_exists "dist/release/"
	assert_file_NOT_exists "bin/dev/"
	assert_file_NOT_exists "doc/userguide/"
	assert_file_NOT_exists "model/"
	assert_file_NOT_exists "src/"
	assert_file_NOT_exists "src-gen/"
	assert_file_NOT_exists "test/"
	assert_file_NOT_exists "kieker-eclipse.importorder"
	assert_file_NOT_exists "kieker-eclipse-cleanup.xml"
	assert_file_NOT_exists "kieker-eclipse-formatter.xml"
	assert_file_NOT_exists ".project"
	assert_file_NOT_exists ".classpath"
	assert_file_NOT_exists ".checkstyle"
	assert_file_NOT_exists ".pmd"
	assert_file_NOT_exists ".settings/"
	assert_file_NOT_exists "doc/README-bin"
	assert_file_NOT_exists "doc/README-src"
}

function check_src_archive {
	if [ -z "$1" ]; then
		echo "No source archive provided"
		exit 1
	fi

	echo "Checking source archives '$1' ..."

	echo "Checking archive contents of '$1' ..."
	ARCHIVE_CONTENT=$(print_archive_contents "$1")
	# TODO: do s.th. with the ${ARCHIVE_CONTENT}

	echo "Decompressing archive '$1' ..."
	extract_archive_n_cd "$1"
	touch $(basename "$1") # just to mark where this dir comes from

	assert_files_exist_src

	# now compile sources
	run_ant
	# make sure that the expected files are present
	assert_dir_exists "dist/"
	assert_file_exists_regular $(ls "dist/kieker-"*".jar" | grep -v emf | grep -v aspectj ) # the core jar
	assert_file_exists_regular "dist/kieker-"*"_aspectj.jar"
	assert_file_exists_regular "dist/kieker-"*"_emf.jar"
	assert_file_NOT_exists "dist/kieker-monitoring-servlet-"*".war"

	# check bytecode version of classes contained in jar
	echo -n "Making sure that bytecode version of class in jar is 49.0 (Java 1.5)"
	MAIN_JAR=$(ls "dist/kieker-"*".jar" | grep -v emf | grep -v aspectj)
	assert_file_exists_regular ${MAIN_JAR}
	VERSION_CLASS=$(find build -name "Version.class")
	assert_file_exists_regular "${VERSION_CLASS}"
	if ! file ${VERSION_CLASS} | grep "version 49.0 (Java 1.5)"; then
		echo "Unexpected bytecode version"
		exit 1
	fi
	echo "OK"

	# now execute junt tests (which compiles the sources again ...)
	run_ant run-tests-junit
	# make sure that no errors occured 
	if ! grep "100.00\%" tmp/junit-results/report/overview-summary.html; then
		echo "The JUnit tests didn't return with 100% success"
		exit 1
	fi

	# now execute junt tests (which compiles the sources again ...)
	run_ant static-analysis
}

function check_bin_archive {
	if [ -z "$1" ]; then
		echo "No source archive provided"
		exit 1
	fi

	echo "Checking source archives '$1' ..."

	echo "Checking archive contents of '$1' ..."
	ARCHIVE_CONTENT=$(print_archive_contents "$1")
	# TODO: do s.th. with the ${ARCHIVE_CONTENT}

	echo "Decompressing archive '$1' ..."
	extract_archive_n_cd "$1"
	touch $(basename "$1") # just to mark where this dir comes from

	assert_files_exist_bin

	# check bytecode version of classes contained in jar
	echo -n "Making sure that bytecode version of class in jar is 49.0 (Java 1.5)"
	MAIN_JAR=$(ls "dist/kieker-"*".jar" | grep -v emf | grep -v aspectj)
	assert_file_exists_regular ${MAIN_JAR}
	VERSION_CLASS_IN_JAR=$(unzip -l	 ${MAIN_JAR} | grep Version.class | awk '{ print $4 }')
	unzip "${MAIN_JAR}" "${VERSION_CLASS_IN_JAR}"
	assert_file_exists_regular "${VERSION_CLASS_IN_JAR}"
	if ! file ${VERSION_CLASS_IN_JAR} | grep "version 49.0 (Java 1.5)"; then
		echo "Unexpected bytecode version"
		exit 1
	fi
	echo "OK"

	# some basic tests with the tools
	if ! (bin/convertLoggingTimestamp.sh --timestamps 1283156545581511026 1283156546127117246 | grep "Mon, 30 Aug 2010 08:22:25 +0000 (UTC)"); then
		echo "Unexpected result executinǵ bin/convertLoggingTimestamp.sh"
		exit 1
	fi
	

	# now perform some trace analysis tests and compare results with reference data
	ARCHDIR=$(pwd)
	create_subdir_n_cd
	REFERENCE_OUTPUT_DIR="${ARCHDIR}/examples/userguide/ch5--trace-monitoring-aspectj/testdata/kieker-20100830-082225522-UTC-example-plots"
	PLOT_SCRIPT="${ARCHDIR}/examples/userguide/ch5--trace-monitoring-aspectj/testdata/kieker-20100830-082225522-UTC-example-plots.sh"
	if ! test -x ${PLOT_SCRIPT}; then
		echo "${PLOT_SCRIPT} does not exist or is not executable"
		exit 1
	fi
	if ! ${PLOT_SCRIPT} "${ARCHDIR}" "."; then # passing kieker dir and output dir
		echo "${PLOT_SCRIPT} returned with error"
		exit 1
	fi
	for f in $(ls "${REFERENCE_OUTPUT_DIR}" | egrep "(dot$|pic$|html$|txt$)"); do 
		echo -n "Comparing to reference file $f ... "
		# Note that this is a hack because sometimes the line order differs
		(cat "$f" | sort) > left.tmp
		(cat "${REFERENCE_OUTPUT_DIR}/$f" | sort) > right.tmp
		if test "$f" = "traceDeploymentEquivClasses.txt" || test "$f" = "traceAssemblyEquivClasses.txt"; then
			# only a basic test because the assignment to classes is not deterministic
			if test -z "$f"; then
				echo "File $f does not exist or is empty"
				exit 1;
			fi
			continue;
		fi
		if ! diff --context=5	 left.tmp right.tmp; then
			echo "Detected deviation between files: '$f', '${REFERENCE_OUTPUT_DIR}/${f}'"
			exit 1
		else 
			echo "OK"
		fi
	done

	# Return to archive base dir
	cd ${ARCHDIR}

	# TODO: test examples ...
}

function assert_no_common_files_in_archives {
 echo "Making sure that archives have no common files: '$1', '$2' ..."

	if [ -z "$2" ]; then
		echo "No source archive provided"
		exit 1
	fi



	CONTENT1_FN="$(basename $1).txt"
	CONTENT2_FN="$(basename $2).txt"

	# excluding directories ("/$") and the zip output header ("Name\n---")
	cat_archive_content $1 | grep -v "/$" | grep -v "^Name" | grep -v "^----" | sort > ${CONTENT1_FN} 
	cat_archive_content $2 | grep -v "/$" | grep -v "^Name" | grep -v "^----" | sort > ${CONTENT2_FN}

	COMMON_CONTENT_FN="common.txt"
	comm -1 -2 ${CONTENT1_FN=} ${CONTENT2_FN} > ${COMMON_CONTENT_FN}
	
	if ! test -f ${COMMON_CONTENT_FN}; then
		echo "File ${COMMON_CONTENT_FN} does not exist"
		exit 1
	fi

	if test -s ${COMMON_CONTENT_FN}; then
		echo "The archives have common files:"
		cat ${COMMON_CONTENT_FN}
		exit 1
	fi
}

##
## "main" 
##

aspectjversion="$(grep "lib.aspectj.version=" build.properties | sed s/.*=//g)"

assert_dir_exists ${BASE_TMP_DIR}
change_dir "${BASE_TMP_DIR}"
BASE_TMP_DIR_ABS=$(pwd)

change_dir "${BASE_TMP_DIR_ABS}"
create_subdir_n_cd
DIR=$(pwd)
SRCZIP=$(ls ../../dist/release/*_sources.zip)
assert_file_exists_regular ${SRCZIP}
check_src_archive "${SRCZIP}"
rm -rf ${DIR}

change_dir "${BASE_TMP_DIR_ABS}"
create_subdir_n_cd
DIR=$(pwd)
SRCTGZ=$(ls ../../dist/release/*_sources.tar.gz)
assert_file_exists_regular ${SRCTGZ}
check_src_archive "${SRCTGZ}"
rm -rf ${DIR}

change_dir "${BASE_TMP_DIR_ABS}"
create_subdir_n_cd
DIR=$(pwd)
BINZIP=$(ls ../../dist/release/*_binaries.zip)
assert_file_exists_regular ${BINZIP}
check_bin_archive "${BINZIP}"
rm -rf ${DIR}

change_dir "${BASE_TMP_DIR_ABS}"
create_subdir_n_cd
DIR=$(pwd)
BINTGZ=$(ls ../../dist/release/*_binaries.tar.gz)
assert_file_exists_regular ${BINTGZ}
check_bin_archive "${BINTGZ}"
rm -rf ${DIR}

# TOOD: check contents of remaining archives
