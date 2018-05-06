# Spectrum Based Fault Localisation: What About Component Tests ?
This repository contains data for the chapter titled above in my PhD thesis. The techniques are evaluated on the [Defects4J](https://github.com/rjust/defects4j) dataset.

# Structure
There are few directories which contain the code / data.

## [Separate_Test_Types](https://github.com/glaghari/sbfl_unit_component_tests/tree/master/Separate_Test_Types)
This directory contains the code that categorises failing tests into either unit tests or component tests, based on the presence of the unit/component tests in the faults the faults are classified as either exposed by unit tests or component tests. Before using this, patch the build file in Defects4J with the code in file `dfj_framework_build_patch_for_tracer.txt`. Then run the tests for with `dfj_scripts/separate_test_types_for_faults` script.

## [tracer](https://github.com/glaghari/sbfl_unit_component_tests/tree/master/tracer)
This directory contains the code for trace / coverage collection. Before using this tracer, patch the build file in Defects4J with the code in file `dfj_framework_build_patch_for_tracer.txt`. Then run the tests for with `dfj_scripts/test` script.

## [ClosedItemsetWithCharm](https://github.com/glaghari/sbfl_unit_component_tests/tree/master/ClosedItemsetWithCharm)
This directory contains the interface code for running the closed itemset mining algorithm on the generated traces. The code closed itemset mining algorithm is used from [SPMF Library](http://www.philippe-fournier-viger.com/spmf/).

## [auto_rankings](https://github.com/glaghari/sbfl_unit_component_tests/tree/master/auto_rankings)
Once the traces are collected by `tracer`, the code in this directory is used to generate the ranked lists. To produce the ranked lists run the script `dfj_scripts/rank`.

## [dfj_scripts](https://github.com/glaghari/sbfl_unit_component_tests/tree/master/dfj_scripts)
This directory contains the scripts related to Defects4J.

`prepareproject` script prepares the directory `testincludes` which contains the files which mark which tests are used for each fault in spectrum based fault Localisation analysis.

`test` script collects the traces.

`rank` script produces the ranked lists.

`separate_test_types_for_faults` script classifies the failing tests into component or unit tests.

`faulty_methods.sh` and `faulty_methods_map.sh` scripts extract faulty methods for the faults in Defects4J. However there are still some manual corrections needed after these scripts generate the data. See `manual_correction_faulty_methods.txt` file.

## [method_extract](https://github.com/glaghari/sbfl_unit_component_tests/tree/master/method_extract)
This directory contains the parser code to extract the faulty methods by `diff`ing and processing the Java code for faulty versions of projects in Defects4J. `faulty_methods.sh` and `faulty_methods_map.sh` scripts use this parser.

## [dfj_diff](https://github.com/glaghari/sbfl_unit_component_tests/tree/master/dfj_diff)
This directory contains the data spitted by the parser, and `faulty_methods.sh` and `faulty_methods_map.sh` scripts.

## [analysis_scripts](https://github.com/glaghari/sbfl_unit_component_tests/tree/master/analysis_scripts)
This directory contains the scripts to analyse the data and generate graphs.

## [Analysis](https://github.com/glaghari/sbfl_unit_component_tests/tree/master/Analysis)
This directory contains the data produced by the artefacts.

### GroundTruth
This directory contains the faulty methods and failing tests for each fault.

### Metrics
This directory contains the calculated metric data provided in the paper.

### Rankings
This directory contains the ranked lists.

### Test_Types
This directory contains the classification of the faults into either exposed by component tests or unit tests.
