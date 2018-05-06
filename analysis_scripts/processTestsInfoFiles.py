#!/usr/bin/python2.7

from os.path import join, exists

projects = ["Time", "Math", "Lang", "Chart", "Closure", "Mockito"]

projects = ["Time", "Math", "Lang", "Chart", "Closure"]

baseDir = "/Users/gulsherlaghari/datasets/dfj/Analysis/Test_Types"

testsFile = join(baseDir, '_tests_info.csv')

with open(testsFile, 'w') as testsFileHandle:
    testsFileHandle.write("project_id,bug_id,#_UT,#_CT,test_type\n")

for projectID in projects:
    print(projectID)
    testsInfoFile = join(baseDir, projectID, "_" + projectID + "_tests_info.csv")

    testsInfo = {}
    with open(testsInfoFile, mode='r') as testsInfoFileHandle:
        for line in testsInfoFileHandle:
            splitLine = line.strip().split(',')
            bugID = int(splitLine[0].replace("b", ""))
            testType = splitLine[2]
            if bugID not in testsInfo:
                testsInfo[bugID] = {"UT": 0, "CT": 0}
            if testType == "CT":
                testsInfo[bugID]["CT"] += 1
            else:
                testsInfo[bugID]["UT"] += 1

    with open(testsFile, 'a') as testsFileHandle:
        for bugID in sorted(testsInfo.keys()):
            CT = 0
            UT = 0
            _type = ""
            if "CT" in testsInfo[bugID]:
                CT = testsInfo[bugID]["CT"]

            if "UT" in testsInfo[bugID]:
                UT = testsInfo[bugID]["UT"]

            if UT > 0:
                _type = "UT"

            elif CT > 0:
                _type = "CT"

            if CT > 0 and UT > 0:
                _type = "CT"
                # _type = "BOTH"

            testsFileHandle.write("%s,%s,%d,%d,%s\n" % (projectID, bugID, UT, CT, _type))

print("Done!")
