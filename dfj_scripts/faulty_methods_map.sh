#!/bin/bash

#---------------------------------------------------------------
# This script first finds method name (complete) from executed method
# by method's name as it appears in source code.
#---------------------------------------------------------------

# Current directory
WORK_DIR=$(pwd)

# Base working directory
BASE_DIR="$WORK_DIR/dfj_diff"

GROUND_TRUTH="$WORK_DIR/Analysis/GroundTruth"
# rm -rf $GROUND_TRUTH
# mkdir -p $GROUND_TRUTH

TRACES="$WORK_DIR/Analysis/Traces"

# Directery used to store faulty methods
METHODS_DIR="$BASE_DIR/faulty_methods"

# Log file for debugging and info
LOG_FILE="$GROUND_TRUTH/log2.txt"

#-----------Helper methods-------------------------------------

#-----------------Check out project versions-------------------
#--------------------------------------------------------------

get_pattern() {
	project="$1"
	bug_id="$2"
	faulty_method="$3"
	strict="$4"
	
	if [[ $strict == "strict" ]]; then
		name=$(cut -f1 -d'(' <<< $faulty_method | sed -E 's/[][$]/\\&/g')
	else
		name=$(sed 's/.*\ //' <<< $faulty_method | cut -f1 -d'(' | sed -E 's/[$]/\\&/g')
		name=".*$name"
	fi

# 	name=$(cut -f1 -d'(' <<< $faulty_method | sed -E 's/[][$]/\\&/g')
	pattern="^[0-9]+,$name\("
	args=$(cut -f2 -d'(' <<< $faulty_method | cut -f1 -d')' | tr ',' '\n')
	for arg in $args
	do
		if [[ $strict == "strict" ]]; then
			arg=$(sed -E 's/[][]/\\&/g' <<< $arg)
		elif [[ $strict == "moderate" ]]; then
			arg=$(sed -E 's/[][]//g' <<< $arg)
			arg="$arg(\[\])*"
		elif [[ $strict == "lenient" ]]; then
			arg=$(sed 's/.*\.//' <<< $arg | sed -E 's/[][]//g')
			arg=".*$arg(\[\])*"
		fi
		
# 		arg=$(sed -E 's/[][]/\\&/g' <<< $arg)
		pattern="$pattern$arg;"
	done
	
	# if there are args then remove last character-- the extra semicolon (;)
	[[ -z $args ]] || pattern=${pattern%?}
	pattern="$pattern\)"
	
	echo $pattern
}

map_method_count() {
	project="$1"
	pattern="$2"
	DB_file="$TRACES/$project/DB.csv"
	mapped_method_count=$(grep -Ec "$pattern" $DB_file)
	echo $mapped_method_count
}

map_method() {
	project="$1"
	pattern="$2"
	DB_file="$TRACES/$project/DB.csv"
	mapped_method=$(grep -E "$pattern" $DB_file)
	echo $mapped_method
}

write_mapped_method() {
	project="$1"
	bug_id="$2"
	mapped_method="$3"
	
	dir="$GROUND_TRUTH/$project/${bug_id}b"
	mkdir -p $dir
	
	faulty_methods_file="$dir/faultyMethods.txt"
	
	if [[ ! -f $faulty_methods_file ]]; then
		echo $bug_id > $faulty_methods_file
	fi
	
	echo $mapped_method >> $faulty_methods_file
}

map2() {
	project="$1"
	pattern="$2"
	faulty_method="$3"
	
# 	echo "2. pattern=[$pattern]"
	
	DB_file="$TRACES/$project/DB.csv"
	grep -E "$pattern" $DB_file | while read -r line
	do
		mapped_method=$(cut -f2 -d',' <<< $line)
		num_args_faulty_method=$(cut -f2 -d'(' <<< $faulty_method \
		| cut -f1 -d')' | tr ',' '\n' | wc -l)
	
		num_args_mapped_method=$(cut -f2 -d'(' <<< $mapped_method | cut -f1 -d')' | tr ';' '\n' | wc -l)					
		if [[ $num_args_faulty_method -eq $num_args_mapped_method ]]; then
			echo "[INFO][$project][$bug_id][MAPPED][$mapped_method]" >> $LOG_FILE
			write_mapped_method $project $bug_id "$mapped_method"
			echo "found"
			# return true
			return 0
		fi
	done
	# return false
	return 1
}

map() {
	project="$1"
	bug_id="$2"
	faulty_methods_file="$METHODS_DIR/$project/$bug_id.txt"
	if [[ ! -f $faulty_methods_file ]]; then
		echo " -> Empty file!"
		echo "[INFO][$project][$bug_id][MAPPED][FAIL] Empty file!" >> $LOG_FILE
		return
	fi
	faulty_methods=$(cat $faulty_methods_file)
	if [[ -n $faulty_methods ]]; then
		cat $faulty_methods_file | while read -r faulty_method
		do
			options=(strict moderate lenient)
			found="no"
			for option in ${options[@]}
			do
				pattern=$(get_pattern $project $bug_id "$faulty_method" $option)
				mapped_method_count=$(map_method_count $project "$pattern")
				if [[ ! $mapped_method_count -eq 0 ]]; then
					found="yes"
					break
				fi
			done

			if [[ $found == "no" ]]; then
				echo " -> Not found! [$faulty_method][$pattern]"
				echo "[INFO][$project][$bug_id][MAPPED][FAIL][$faulty_method] Not found!" >> $LOG_FILE
				continue
			fi

			if [[ $mapped_method_count -eq 1 ]]; then
# 				echo "1. pattern=[$pattern]"
				mapped_method=$(map_method $project "$pattern")
				mapped_method=$(cut -f2 -d',' <<< $mapped_method)
# 				echo $mapped_method
				echo "[INFO][$project][$bug_id][MAPPED][$mapped_method]" >> $LOG_FILE
				write_mapped_method $project $bug_id "$mapped_method"
				continue
			elif [[ $mapped_method_count -gt 1 ]]; then
				if [[ ! $(map2 $project "$pattern" "$faulty_method") ]]; then
					mapped_methods=$(map_method $project "$pattern")
					echo " -> MANUAL-CHECK [$faulty_method][$pattern][$mapped_methods]"
					echo "[INFO][$project][$bug_id][MAPPED][MANUAL-CHECK][$faulty_method] \
					More than one mapped methods![$mapped_methods]" >> $LOG_FILE
				else
					echo " -> RESOLVED DOUBLE-CHECK"
				fi
			 fi
		done
	else
		echo " -> No methods in file!"
		echo "[INFO][$project][$bug_id][MAPPED][FAIL] No methods in file!" >> $LOG_FILE
	fi
}

#-----------------Write failing tests--------------------------
#--------------------------------------------------------------

write_failing_tests() {
	project="$1"
	bug_id="$2"

	dir="$GROUND_TRUTH/$project/${bug_id}b"
	
	[[ -d $dir ]] || return
	
	failing_tests_file="$dir/failing_tests.txt"
	
	# Write failing tests, Change :: -> #
	grep '^--- ' "defects4j/framework/projects/$project/trigger_tests/$bug_id" \
	| cut -f2 -d' ' 1> $failing_tests_file 2> /dev/null
	
	num_failing_tests=$(grep -c '^--- ' "defects4j/framework/projects/$project/trigger_tests/$bug_id")
	echo "[INFO][$project][$bug_id][#FAIL-TESTS][$num_failing_tests]" >> $LOG_FILE
}


#-----------------Run the whole pipeline---------------------------
#------------------------------------------------------------------

# All projects
projects=(Chart Time Lang Closure Math)
# projects=(Mockito)

echo "[INFO][START]" > $LOG_FILE
for project in ${projects[@]}
do
	rm -rf "$GROUND_TRUTH/$project" 
	mkdir -p "$GROUND_TRUTH/$project"
	
	# Total bugs in current project
	total_bugs=$(grep -c "$" "defects4j/framework/projects/$project/commit-db")
	start_bug_id=1
# 	total_bugs=1

# 	for bug_id in 30 32 34 47 64 98 123
	for bug_id in $(seq $start_bug_id $total_bugs)
	do
		echo "[INFO][$project][$bug_id][MAP][START]" >> $LOG_FILE
		echo
		echo -n "$project/$bug_id"

		map $project $bug_id
		echo "[INFO][$project][$bug_id][MAP][END]" >> $LOG_FILE
		
		# Write failing tests
		write_failing_tests $project $bug_id

	done
done
echo "[INFO][END]" >> $LOG_FILE
