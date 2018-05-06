#!/bin/bash

#---------------------------------------------------------------
# This script first finds the changed lines (either modified or 
# the first line of inserted lines in the class. Then it writes
# the method's name as it appears in source code.
#---------------------------------------------------------------



# Current directory
WORK_DIR=$(pwd)

# Base working directory
BASE_DIR="$WORK_DIR/dfj_diff"






rm -rf $BASE_DIR






mkdir -p $BASE_DIR

# Directory used to checkout project sources
PROJECTS_SRC_DIR="$BASE_DIR/projects_src_dir"

# Directery used to store changed lines
CHANGED_LINES_DIR="$BASE_DIR/changed_lines"

# Directery used to store faulty methods
METHODS_DIR="$BASE_DIR/faulty_methods"

# Log file for debugging and info
LOG_FILE="$BASE_DIR/log.txt"

# Defects4j runtime
defects4j=defects4j/framework/bin/defects4j


#-----------Helper methods-------------------------------------

#-----------------Check out project versions-------------------
#--------------------------------------------------------------
		
checkout() {

	project_dir="$1"
	project="$2"
	bug_id="$3"
	
# 	rm -rf $project_dir
	mkdir -p $project_dir

	echo "[INFO][$project][$bug_id][CHECKOUT] checking out buggy and fixed versions" >> $LOG_FILE

	# Checkout buggy project version
	buggy_dir="$project_dir/${bug_id}b"
	$defects4j checkout -p$project -v${bug_id}b -w$buggy_dir > /dev/null 2>&1

	# Checkout fixed project version
	fixed_dir=$project_dir/${bug_id}f
	$defects4j checkout -p$project -v${bug_id}f -w$fixed_dir > /dev/null 2>&1
}

#-----------------Find changed lines of modified classes-------
#--------------------------------------------------------------		

find_changed_lines() {
	project_dir="$1"
	project="$2"
	bug_id="$3"

	# Source directory
	classes_src_dir=$(grep "d4j.dir.src.classes=" $buggy_dir/defects4j.build.properties | cut -f2 -d'=')

	# Classes modified to fix the bug
	modified_classes=$(cat defects4j/framework/projects/$project/modified_classes/$bug_id.src)
	num_modified_classes=$(grep -c '.$' <<< $modified_classes)
	echo "[INFO][$project][$bug_id][MODIFIED-CLASSES][$num_modified_classes][$(tr '\n' ':' <<< $modified_classes)]" >> $LOG_FILE

	changed_lines_file=$changed_lines_dir/$bug_id.txt

	# For each modified class find the changed lines
	for modified_class in $modified_classes
	do
		modified_class_file="$(sed 's/\./\//g' <<< "$modified_class").java"
		source_class_file="$buggy_dir/$classes_src_dir/$modified_class_file"
		target_class_file="$fixed_dir/$classes_src_dir/$modified_class_file"
		echo "[INFO][$project][$bug_id][DIFF][$source_class_file][$target_class_file]" >> $LOG_FILE

		# Diff between buggy and fixed classes, only write class path and changed line numbers
		changed_lines=$(
		diff \
		        --old-line-format=",%dn" \
		        --new-group-format=",%df" \
		        --changed-group-format=",%df%(f=l?:,%dl)" \
		        --unchanged-group-format="" \
		        $source_class_file $target_class_file
		        )

		if [[ 1 -eq $(grep -Ec "^(,[0-9]+)+$" <<< $changed_lines) ]]; then
			echo "$classes_src_dir/$modified_class_file$changed_lines" >> $changed_lines_file
		fi

	done
}

#-----------------Check on changed lines of modified classes---
#--------------------------------------------------------------
		
check_changed_lines() {
	changed_lines_file="$1"

	if [[ ! -f $changed_lines_file ]]; then
		echo "[WARN][$project][$bug_id][SKIP][CHECK-CHANGED-LINES][No changed lines file!]" >> $LOG_FILE
		# return true
		return 0
	fi

	if [[ ! -s $changed_lines_file ]]; then
		echo "[WARN][$project][$bug_id][SKIP][CHECK-CHANGED-LINES][Changed lines file empty!][No diff found]" >> $LOG_FILE
		# return true
		return 0
	fi

	num_changed_lines_classes=$(grep -c '.$' $changed_lines_file)
	changed_lines_classes=$(cat $changed_lines_file | tr '\n' ':')
	echo "[INFO][$project][$bug_id][CHECK-CHANGED-LINES][$changed_lines_classes]" >> $LOG_FILE

	num_modified_classes=$(grep -c '.$' defects4j/framework/projects/$project/modified_classes/$bug_id.src)

	#If changed lines not found for all modified classes, there is problem
	if [[ ! $num_modified_classes -eq $num_changed_lines_classes ]]; then
		echo "[DEBUG][$project][$bug_id][CHECK-CHANGED-LINES][MISMATCH-MODIFIED-CLASSES][$num_modified_classes][$num_changed_lines_classes]" >> $LOG_FILE
		echo "[WARN][$project][$bug_id][SKIP][CHECK-CHANGED-LINES][Perhaps changed lines not obtained for all modified classes]" >> $LOG_FILE
		# return true
		return 0
	fi

	# return false
	return 1	
}

#-----------------Find faulty statements and methods-----------
#--------------------------------------------------------------

extract_faulty_entities() {
	project="$1"
	bug_id="$2"
	changed_lines_file="$3"
	
	buggy_dir="$PROJECTS_SRC_DIR/$project/${bug_id}b"
	# Source directory
	classes_src_dir=$(grep "d4j.dir.src.classes=" $buggy_dir/defects4j.build.properties | cut -f2 -d'=')

	pushd method_extract > /dev/null 2>&1
	java -cp ".:lib/antlr-4.7-complete.jar" extract.ExtractEntityTool \
	$PROJECTS_SRC_DIR \
	$project \
	$bug_id \
	$classes_src_dir \
	$changed_lines_file \
	$METHODS_DIR
	popd > /dev/null 2>&1
}

#-----------------Check on faulty statements and methods-----------
#------------------------------------------------------------------

check_faulty_entities() {
	project="$1"
	bug_id="$2"
	entity="$3" # STATEMENT / METHOD
	entity_dir="$4"

	faulty_entity_file="$entity_dir/$project/$bug_id.txt"

	if [[ ! -f $faulty_entity_file ]]; then
		echo "[WARN][$project][$bug_id][SKIP][CHECK-FAULTY-ENTITIES][$entity][No faulty file!]" >> $LOG_FILE
		# return true
		return 0
	fi

	if [[ ! -s $faulty_entity_file ]]; then
		echo "[WARN][$project][$bug_id][SKIP][CHECK-FAULTY-ENTITIES][$entity][Faulty file empty!][No diff found]" >> $LOG_FILE
		# return true
		return 0
	fi

	num_faulty_entities=$(grep -c '.$' $faulty_entity_file)
	faulty_entities=$(cat $faulty_entity_file | tr '\n' ':')

	echo "[INFO][$project][$bug_id][CHECK-FAULTY-ENTITIES][FAULTY-${entity}S][$num_faulty_entities][$faulty_entities]" >> $LOG_FILE

	# return false
	return 1
}

#-----------------Run the whole pipeline---------------------------
#------------------------------------------------------------------

# All projects
projects=(Chart Time Lang Math Mockito Closure)
# projects=(Closure)

echo "[INFO][START][$(date)]" > $LOG_FILE

for project in ${projects[@]}
do
	echo "[INFO][$project][START-PROCESS][$(date)]" >> $LOG_FILE
	# Current project working dir
	project_dir="$PROJECTS_SRC_DIR/$project"
	# mkdir -p $project_dir

	# Changed lines dir used to store changed lines in buggy classes
	changed_lines_dir="$CHANGED_LINES_DIR/$project"
# 	rm -rf $changed_lines_dir
# 	rm -rf "$METHODS_DIR/$project"
	mkdir -p $changed_lines_dir

	# Total bugs in current project
	total_bugs=$(grep -c "$" "defects4j/framework/projects/$project/commit-db")
	start_bug_id=1
# 	total_bugs=1

# 	bugs=(30 32 34 47 64 98 123)
# 	for bug_id in ${bugs[@]}
	for bug_id in $(seq $start_bug_id $total_bugs)
	do
		echo "$project/$bug_id"
		# Checkout projects
		checkout $project_dir $project $bug_id

		# Write failing tests
		#write_failing_tests $project_dir $project $bug_id

		# Find changed lines and classes
		find_changed_lines $project_dir $project $bug_id

		# Check if changed lines found
		changed_lines_file=$changed_lines_dir/$bug_id.txt
		if check_changed_lines $changed_lines_file; then
			continue
		fi

		# Extract faulty methods
		extract_faulty_entities $project $bug_id $changed_lines_file

		# Check on faulty methods
		if check_faulty_entities $project $bug_id "METHOD" $METHODS_DIR; then
			continue
		fi
		
		# Clean project code
		rm -rf $project_dir

		echo "[INFO][$project][END-PROCESS][$(date)]" >> $LOG_FILE

	done
	
done

rm -rf $PROJECTS_SRC_DIR

echo "[INFO][END][$(date)]" >> $LOG_FILE
