#!/usr/bin/python2.7

from __future__ import print_function
import random
from os.path import join, exists, isfile
from os import makedirs, listdir, remove, rename
from shutil import rmtree
import numpy as np
from scipy import stats


def random_sort(ranking):
    random.seed(50)
    mp = {}
    for p in ranking.items():
        s = p[0]
        v = p[1]
        if v not in mp:
            mp[v] = list()
        mp[v] += [s]

    keys = sorted(mp.keys(), reverse=True)
    random_sorted = []
    for k in keys:
        lst = mp[k]
        lst = sorted(lst)
        random.shuffle(lst)
        random_sorted += [(e, k) for e in lst]

    return random_sorted


def hit_at_n(random_sorted_ranking, faulty_ids_list, top_n):
    for i in xrange(len(random_sorted_ranking)):
        if i >= top_n:
            break
        tuple_method_score = random_sorted_ranking[i]
        method_id = tuple_method_score[0]
        if method_id in faulty_ids_list:
            return 1
    return 0


def average_precision(random_sorted_ranking, faulty_ids_list):
    count = 0
    found = 0
    precision_list = []
    for tuple_method_score in random_sorted_ranking:
        count += 1
        method_id = tuple_method_score[0]
        if method_id in faulty_ids_list:
            found += 1
            precision = float(found) / float(count)
            precision_list += [precision]

    if len(precision_list) == 0:
        print("ERROR: no matches in average precisions")
        print(faulty_ids_list)
        return None

    return np.mean(precision_list)


def wasted_effort(random_sorted_ranking, faulty_ids_list):
    faulty_entity = None
    for tuple_method_score in random_sorted_ranking:
        method_id = tuple_method_score[0]
        suspiciousness = tuple_method_score[1]
        if method_id in faulty_ids_list:
            faulty_entity = tuple_method_score if faulty_entity is None else tuple_method_score if suspiciousness > faulty_entity[1] else faulty_entity

    if faulty_entity is None:
        return None

    m = 0  # For number of entities with higher suspiciousness
    n = 0  # For number of entities with same suspiciousness.

    for tuple_method_score in random_sorted_ranking:
        method_id = tuple_method_score[0]
        suspiciousness = tuple_method_score[1]

        if method_id in faulty_ids_list:
            continue

        m += 1 if suspiciousness > faulty_entity[1] else 0
        n += 1 if suspiciousness == faulty_entity[1] else 0

    return float(m) + float(n / 2)


def rank(random_sorted_ranking, faulty_ids_list):
    _rank = 0
    for tuple_method_score in random_sorted_ranking:
        _rank += 1
        method_id = tuple_method_score[0]
        if method_id in faulty_ids_list:
            return _rank

    return _rank


def test():
    ranking = {1: 0.6, 2: 0.9, 3: 0.4, 4: 0.3, 5: 0.6}
    print(ranking)
    random_sorted = random_sort(ranking)
    print(random_sorted)
    faulty_ids_list = [1]
    metrics = []
    for i in [1, 3, 5]:
        metrics += [hit_at_n(random_sorted, faulty_ids_list, i)]

    print(faulty_ids_list)
    avg_p = average_precision(random_sorted, faulty_ids_list)
    metrics += [avg_p]
    print(avg_p)
    print(metrics)
    line = ''
    for v in metrics:
        line += str(v) + COMMA

    line = line[:len(line)-1]
    print(line)
    line = repr(metrics)
    line = line[1:len(line)-1]
    print(line)


def get_faulty_method_list(faulty_methods_file, DB_file):
    faulty_methods_list = [line.strip() for line in open(faulty_methods_file) if line.__contains__('(') and len(line.strip()) > 0]
    DB_dict = {}
    with open(DB_file) as DB_file_handler:
        for line in DB_file_handler:
            line_list = line.strip().split(COMMA)
            if len(line_list) == 2:
                DB_dict.update({line_list[1].strip(): int(line_list[0].strip())})

    faulty_ids_list = [DB_dict[faulty_method] for faulty_method in faulty_methods_list if faulty_method in DB_dict]
    return faulty_ids_list


def calc_metrics():
    print('\nCALCULATE')
    if not exists(join(base_dir, METRICS)):
        makedirs(join(base_dir, METRICS))

    rank_summary_file = join(base_dir, METRICS, RANK_SUMMARY)
    with open(rank_summary_file, 'w') as rank_summary_file_handler:
        rank_summary_file_handler.write('bug_id,rank,fault_locator,project,analysis\n')

    for ranking_type, ranking_sub_types in ranking_types.iteritems():
        print(ranking_type)
        for ranking_sub_type in ranking_sub_types:
            print('-' + ranking_sub_type + '->')
            for project, bugs in projects.iteritems():
                file_path_dir = join(base_dir, METRICS, ranking_type, ranking_sub_type, project)
                rank_file_path_dir = join(file_path_dir, RANK)
                create_dirs(file_path_dir, rank_file_path_dir)

                print('\tProject -> %s' % project)
                bug_rank_dict = {}
                process_rankings(bug_rank_dict, bugs, file_path_dir, project, ranking_sub_type, ranking_type)
                write_rank_summary(bug_rank_dict, project, rank_summary_file, ranking_sub_type, ranking_type)
                write_ranks_summary(bug_rank_dict, rank_file_path_dir)


def write_rank_summary(bug_rank_dict, project, rank_summary_file, ranking_sub_type, ranking_type):
    analysis = get_analysis_type(ranking_sub_type, ranking_type)
    with open(rank_summary_file, 'a') as rank_summary_file_handler:
        for bug_id, rank_dict in bug_rank_dict.iteritems():
            for fault_locator, rank in rank_dict.iteritems():
                rank_summary_file_handler.write('%d,%d,%s,%s,%s\n' % (bug_id, rank, fault_locator, project, analysis))


def get_analysis_type(ranking_sub_type, ranking_type):
    if ranking_type == RAW_SPECTRUM:
        return 'Basic Spectrum Analysis'
    elif ranking_sub_type == 'patterns':
        return 'Extended Spectrum Analysis'
    else:
        return 'Sequenced Spectrum Analysis'


def create_dirs(file_path_dir, rank_file_path_dir):
    if exists(file_path_dir):
        rmtree(file_path_dir)
    makedirs(file_path_dir)
    if exists(rank_file_path_dir):
        rmtree(rank_file_path_dir)
    makedirs(rank_file_path_dir)


def process_rankings(bug_rank_dict, bugs, file_path_dir, project, ranking_sub_type, ranking_type):
    for i in xrange(1, bugs + 1):
        if ranking_sub_type:
            ranking_file = join(base_dir, RANKINGS_DIR, project, str(i) + 'b', ranking_type, ranking_sub_type, str(i) + '_' + ranking_sub_type + RANKINGS_FILE)
        else:
            ranking_file = join(base_dir, RANKINGS_DIR, project, str(i) + 'b', ranking_type, str(i) + RANKINGS_FILE)

        if exists(ranking_file):
            rankings_dict = {}
            fault_locators = read_ranking_file(ranking_file, rankings_dict)

            faulty_methods_file = join(base_dir, GROUND_TRUTH_DIR, project, str(i) + 'b', FAULTY_METHOD_FILE)
            DB_file = join(base_dir, TRACES_DIR, project, DB_FILE)
            faulty_ids_list = get_faulty_method_list(faulty_methods_file, DB_file)
            if not faulty_ids_list:
                print('%d -> Faulty ID Not found' % i)

            calculate_metrics(bug_rank_dict, fault_locators, faulty_ids_list, file_path_dir, i, rankings_dict)

        else:
            print('\t\t\t\tExcluded %d' % i)
            excluded[project].add(i)


def calculate_metrics(bug_rank_dict, fault_locators, faulty_ids_list, file_path_dir, i, rankings_dict):
    bug_rank_dict[i] = {}
    create_fault_locator_files(fault_locators, file_path_dir)
    for fault_locator, ranking in rankings_dict.iteritems():
        random_sorted = random_sort(ranking)
        metrics = []
        for top in [1, 3, 5]:
            metrics += [hit_at_n(random_sorted, faulty_ids_list, top)]

        metrics += [average_precision(random_sorted, faulty_ids_list)]
        if metrics[-1] == None:
            print(i)

        metrics += [wasted_effort(random_sorted, faulty_ids_list)]

        file_path = join(file_path_dir, fault_locator + CSV_EXTENSION)
        line = repr(metrics)[1: -1]
        with open(file_path, 'a') as _file_handler:
            _file_handler.write(str(i))
            _file_handler.write(COMMA)
            _file_handler.write(line)
            _file_handler.write('\n')

        bug_rank = rank(random_sorted, faulty_ids_list)
        bug_rank_dict[i][fault_locator] = bug_rank


def create_fault_locator_files(fault_locators, file_path_dir):
    for fault_locator in fault_locators:
        if fault_locator == METHOD_ID:
            continue

        file_path = join(file_path_dir, fault_locator + CSV_EXTENSION)

        # If file exists, some data has already been written
        if not exists(file_path):
            line = HEADER_LINE
            with open(file_path, 'w') as _file_handler:
                _file_handler.write(line)
                _file_handler.write('\n')


def read_ranking_file(ranking_file, rankings_dict):
    with open(ranking_file) as _file_handler:
        header_line = _file_handler.readline()
        fault_locators = header_line.strip().split(COMMA)
        fault_locators2 = [fault_locators[0]]

        for fault_locator in fault_locators:
            if (fault_locator + CSV_EXTENSION) in file_includes:
                fault_locators2.append(fault_locator)

        fault_locators = fault_locators2
        method_index = fault_locators.index(METHOD_ID)
        for fault_locator_index in xrange(method_index + 1, len(fault_locators)):
            rankings_dict[fault_locators[fault_locator_index]] = {}

        rankings_list = []
        for line in _file_handler:
            rankings_line = line.strip().split(COMMA)
            rankings_list.append(rankings_line)

        fault_locator_list = list(rankings_dict.keys())
        for l in rankings_list:
            for f in fault_locator_list:
                fault_locator_index = fault_locators.index(f)
                rankings_dict[f].update({int(l[0]): float(l[fault_locator_index])})
    return fault_locators


def write_ranks_summary(bug_rank_dict, rank_file_path_dir):
    fault_locator_list = []
    header = BUG_ID + COMMA
    for fault_locator in sorted(bug_rank_dict[1].keys()):
        fault_locator_list.append(fault_locator)
        header += fault_locator + COMMA

    header = header[0:-1] + '\n'
    file_path = join(rank_file_path_dir, SUMMARY)

    with open(file_path, 'w') as file_path_handler:
        file_path_handler.write(header)

    with open(file_path, 'a') as file_path_handler:
        for key in sorted(bug_rank_dict.keys()):
            line = '%s,' % str(key)
            for fault_locator in fault_locator_list:
                line += '%d,' % bug_rank_dict[key][fault_locator]
            line = '%s\n' % line[0: -1]
            file_path_handler.write(line)


def summary_metrics():
    print('\nSUMMARY TEST TYPES')
    summary_metrics_dict = summarize()
    write_summarized(summary_metrics_dict)


def write_summarized(summary_metrics_dict):
    for ranking_type, ranking_sub_type_dict in summary_metrics_dict.iteritems():
        for ranking_sub_type, project_dict in ranking_sub_type_dict.iteritems():
            all_project_summary = {}
            for project, fault_locator_dict in project_dict.iteritems():
                file_path_dir = join(base_dir, METRICS, ranking_type, ranking_sub_type, project)
                if fault_locator_dict:
                    for test_type in test_types:
                        if exists(join(file_path_dir, test_type)):
                            rmtree(join(file_path_dir, test_type))
                        makedirs(join(file_path_dir, test_type))

                write_fault_locator_summary(fault_locator_dict, file_path_dir)
                summarize_total(all_project_summary, fault_locator_dict, project)
                file_path_dir = join(base_dir, METRICS, ranking_type, ranking_sub_type)
                write_fault_locator_summary(all_project_summary, file_path_dir)


def summarize_total(all_project_summary, fault_locator_dict, project):
    for fault_locator, test_type_dict in fault_locator_dict.iteritems():
        all_project_summary[fault_locator] = all_project_summary.get(fault_locator, {})
        for test_type, test_type_dict in test_type_dict.iteritems():
            if test_type in test_types:
                continue
            all_project_summary[fault_locator][test_type] = all_project_summary[fault_locator].get(test_type, {})
            for metric, metrics_dict in test_type_dict.iteritems():
                metric_dict = all_project_summary[fault_locator][test_type].get(metric, None)
                if metric_dict is None:
                    all_project_summary[fault_locator][test_type][metric] = {}
                    metric_dict = all_project_summary[fault_locator][test_type][metric]

                for bug_id, metric_value in metrics_dict.iteritems():
                    metric_dict.update({project + str(bug_id): metric_value})


def write_fault_locator_summary(fault_locator_dict, file_path_dir):
    summary_file = join(file_path_dir, SUMMARY)
    if exists(summary_file):
        remove(summary_file)

    for fault_locator, test_type_dict in fault_locator_dict.iteritems():
        for test_type, metrics_dict in test_type_dict.iteritems():
            header = []
            for key in metrics_dict.keys():
                header.append(key)

            header.sort()
            header_line = repr(header)[1:len(repr(header)) - 1]
            header_line = header_line.replace('\'', '').replace(' ', '')

            write_test_type_fault_locator_file(fault_locator, file_path_dir, header, header_line, metrics_dict, test_type)
            write_summary_file(fault_locator, file_path_dir, header, header_line, metrics_dict, test_type)


def write_test_type_fault_locator_file(fault_locator, file_path_dir, header, header_line, metrics_dict, test_type):
    if not metrics_dict[header[0]]:
        return

    if test_type in test_types:
        _file = join(file_path_dir, test_type, fault_locator)
        if not exists(_file):
            with open(_file, 'w') as _file_handler:
                _file_handler.write(BUG_ID + COMMA + header_line + NEW_LINE)

        with open(_file, 'a') as _file_handler:
            for bug_id in metrics_dict[header[0]].keys():
                score_list = []
                for key in header:
                    score = metrics_dict[key][bug_id]
                    if key.startswith('acc'):
                        score = int(score)
                    else:
                        score = float(score)

                    score_list.append(score)

                line = '%s, %s\n' % (str(bug_id), repr(score_list)[1: -1])
                _file_handler.write(line)


def write_summary_file(fault_locator, file_path_dir, header, header_line, metrics_dict, test_type):
    _file = join(file_path_dir, test_type, SUMMARY)
    if not metrics_dict[header[0]]:
        return

    if not exists(_file):
        header_line = header_line.replace(AVERAGE_PRECISION, MEAN_AVERAGE_PRECISION)
        header_line = header_line.replace(WASTED_EFFORT, MEAN_WASTED_EFFORT)
        with open(_file, 'w') as _file_handler:
            _file_handler.write('fault_locator,' + header_line + '\n')

    score_list = []
    for key in header:
        scores = metrics_dict[key].values()
        if key == AVERAGE_PRECISION:
            mean_average_precision = np.mean(scores)
            score_list.append(mean_average_precision)
        elif key == WASTED_EFFORT:
            mean_wasted_effort = np.mean(scores)
            score_list.append(mean_wasted_effort)
        else:
            _sum = int(np.sum(scores))
            score_list.append(_sum)
    with open(join(file_path_dir, test_type, SUMMARY), 'a') as _file_handler:
        _file_handler.write(fault_locator[:-4] + ', ' + repr(score_list)[1: -1] + '\n')


def summarize():
    summary_metrics_dict = {}
    tests_info_dict = read_tests_info()
    print(tests_info_dict.keys())
    for project, bug_ids_dict in tests_info_dict.iteritems():
        print('\t%-8s -> ' % project, end='')
        total = 0
        for test_type, bug_IDs in bug_ids_dict.iteritems():
            total += len(bug_IDs)
            print('%s=%2d ' % (test_type, len(bug_IDs)), end='')

        print('Total=%3d' % total)

    for ranking_type, ranking_sub_types in ranking_types.iteritems():
        print(ranking_type)
        summary_metrics_dict[ranking_type] = {}
        for ranking_sub_type in ranking_sub_types:
            print('-' + ranking_sub_type + '->')
            summary_metrics_dict[ranking_type][ranking_sub_type] = {}
            metrics_dict = summary_metrics_dict[ranking_type][ranking_sub_type]
            file_path_dir = join(base_dir, METRICS, ranking_type, ranking_sub_type)
            process_metrics(metrics_dict, tests_info_dict, file_path_dir)
    return summary_metrics_dict


def process_metrics(metrics_dict, tests_info_dict, file_path_dir):
    for project, bugs in projects.iteritems():
        project_path_dir = join(file_path_dir, project)
        print('\tProject -> %s' % project)
        all_metrics = [f for f in listdir(project_path_dir) if f in file_includes and isfile(join(project_path_dir, f))]
        metrics_dict[project] = {}

        for fault_locator in all_metrics:
            fault_locator_file = join(project_path_dir, fault_locator)
            if exists(fault_locator_file):
                metrics_dict[project][fault_locator] = {}
                fault_locator_metrics_dict = metrics_dict[project][fault_locator]
                fault_locator_metrics_dict[''] = {}
                for test_type in test_types:
                    fault_locator_metrics_dict[test_type] = {}

                read_fault_locator_file(project, fault_locator_file, fault_locator_metrics_dict, tests_info_dict)


def read_fault_locator_file(project, fault_locator_file, fault_locator_metrics_dict, tests_info_dict):
    with open(fault_locator_file, 'r') as _file_handler:
        header_line = _file_handler.readline()
        headers = header_line.strip().split(COMMA)
        for i in xrange(1, len(headers)):
            metric = headers[i]
            fault_locator_metrics_dict[''][metric] = {}
            for test_type in test_types:
                fault_locator_metrics_dict[test_type][metric] = {}

        for line in _file_handler:
            rankings_line = line.strip().split(COMMA)
            bug_id = int(rankings_line[0])

            for test_type in test_types:
                if bug_id in tests_info_dict[project][test_type]:
                    break

            if test_type not in test_types:
                test_type = "UNKNOWN"

            for i in xrange(1, len(headers)):
                metric = headers[i]
                score = float(rankings_line[i])
                fault_locator_metrics_dict[''][metric].update({bug_id: score})
                fault_locator_metrics_dict[test_type][metric].update({bug_id: score})


def read_tests_info():
    tests_info_dict = {}
    tests_info_file = join(base_dir, 'Test_Types', '_tests_info.csv')
    if not exists(tests_info_file):
        print('Not found %s' % tests_info_file)
        exit()

    with open(tests_info_file, 'r') as tests_info_file_handler:
        # tests_info_file_handler.readline()  # Discard headers
        for line in tests_info_file_handler:
            if line.startswith('project_id'):
                continue

            tests_info = line.strip().split(COMMA)
            project = tests_info[0]
            bug_id  = int(tests_info[1])

            # Don't count excluded bugs
            if project in excluded and bug_id in excluded[project]:
                continue

            test_type = tests_info[-1].strip()

            if project not in tests_info_dict:
                tests_info_dict[project] = {test_type: set()}

            if test_type not in tests_info_dict[project]:
                tests_info_dict[project][test_type] = set()

            tests_info_dict[project][test_type].add(bug_id)
    return tests_info_dict


def rank_fault_locators():
    _test_types = test_types
    _test_types.append('')
    for ranking_type, ranking_sub_types in ranking_types.iteritems():
        for ranking_sub_type in ranking_sub_types:
            for project, bugs in projects.iteritems():
                for test_type in _test_types:
                    file_path_dir = join(base_dir, METRICS, ranking_type, ranking_sub_type, project, test_type)
                    summary_fault_locators_dict = read_fault_locator_summary(project, ranking_sub_type, ranking_type, test_type, file_path_dir)
                    final_rank = compute_ranking(summary_fault_locators_dict)

                    summary_file = join(file_path_dir, SUMMARY)
                    summary_file_tmp = join(file_path_dir, SUMMARY_TMP)
                    if exists(summary_file):
                        with open(summary_file_tmp, 'w') as _tmp_file_handler:
                            with open(summary_file) as _file_handler:
                                header_line = _file_handler.readline().strip()
                                _tmp_file_handler.write(header_line)
                                _tmp_file_handler.write(COMMA + RANK + NEW_LINE)
                                for line in _file_handler:
                                    line = line.strip()
                                    fault_locator = line.split(COMMA)[0]
                                    _rank = final_rank[fault_locator]
                                    _tmp_file_handler.write(line)
                                    _tmp_file_handler.write(',%s\n' % str(_rank))
                    if exists(summary_file):
                        remove(summary_file)
                        rename(summary_file_tmp, summary_file)

    # Rank totals
    for ranking_type, ranking_sub_types in ranking_types.iteritems():
        for ranking_sub_type in ranking_sub_types:
            test_type = ''
            project = ''
            file_path_dir = join(base_dir, METRICS, ranking_type, ranking_sub_type, project, test_type)
            summary_fault_locators_dict = read_fault_locator_summary(project, ranking_sub_type, ranking_type, test_type, file_path_dir)
            final_rank = compute_ranking(summary_fault_locators_dict)

            summary_file = join(file_path_dir, SUMMARY)
            summary_file_tmp = join(file_path_dir, SUMMARY_TMP)
            if exists(summary_file):
                with open(summary_file_tmp, 'w') as _tmp_file_handler:
                    with open(summary_file) as _file_handler:
                        header_line = _file_handler.readline().strip()
                        _tmp_file_handler.write(header_line)
                        _tmp_file_handler.write(COMMA + RANK + NEW_LINE)
                        for line in _file_handler:
                            line = line.strip()
                            fault_locator = line.split(COMMA)[0]
                            _rank = final_rank[fault_locator]
                            _tmp_file_handler.write(line)
                            _tmp_file_handler.write(',%s\n' % str(_rank))
            if exists(summary_file):
                remove(summary_file)
                rename(summary_file_tmp, summary_file)


def read_fault_locator_summary(project, ranking_sub_type, ranking_type, test_type, file_path_dir):
    summary_fault_locators_dict = {}
    summary_file = join(file_path_dir, SUMMARY)
    if exists(summary_file):
        with open(summary_file) as _file_handler:
            header_line = _file_handler.readline()
            headers = header_line.strip().split(COMMA)

            for i in xrange(1, len(headers)):
                metric = headers[i].strip()
                summary_fault_locators_dict[metric] = {}

            for line in _file_handler:
                metrics_line = line.strip().split(COMMA)
                fault_locator = metrics_line[0]
                for i in xrange(1, len(headers)):
                    score = float(metrics_line[i])
                    metric = headers[i].strip()
                    summary_fault_locators_dict[metric].update({fault_locator: score})
    return summary_fault_locators_dict


def compute_ranking(summary_fault_locators_dict):
    final_rank = {}
    reverse = True
    for metric, fault_locators_dict in sorted(summary_fault_locators_dict.iteritems()):
        if metric == MEAN_WASTED_EFFORT:
            _reverse = False
        else:
            _reverse = True

        _rank = 0.0
        prev_value = 0.0

        for k, v in sorted(fault_locators_dict.items(), key=lambda x: x[1], reverse=_reverse):
            if k not in final_rank:
                final_rank.update({k: []})

            if v != prev_value:
                _rank += 1

            final_rank[k].append(_rank)
            prev_value = v
    for metric in final_rank.keys():
        final_rank[metric] = np.mean(final_rank[metric])

    _rank = 0
    prev_value = 0
    for k, v in sorted(final_rank.items(), key=lambda x: x[1]):
        if v != prev_value:
            _rank += 1

        final_rank[k] = _rank
        prev_value = v

    return final_rank


def sum_time(_file):
    h = 0
    m = 0
    s = 0
    with open(_file) as _file_handler:
        for line in _file_handler:
            if 'Time taken' in line:
                line = line.strip().split('Time taken ')[-1].split(':')
                h += int(line[0])
                m += int(line[1])
                s += int(line[2])

    _m, _s = divmod(s, 60)
    m += _m
    s = _s
    _h, _m = divmod(m, 60)
    h += _h
    m = _m
    print('%d:%d:%d [hours:minutes:seconds]' % (h, m, s))


def separate_ranks_into_test_types():
    tests_info_dict = read_tests_info()
    base = join(base_dir, 'Metrics')

    in_file = join(base, 'rank_summary.csv')
    out_file = join(base, 'rank_summary_test_types.csv')

    file_data = []

    with open(in_file, 'r') as in_file_handeler:
        headers = in_file_handeler.readline().strip()
        headers += ',fault_type\n'
        for line in in_file_handeler:
            line = line.strip()
            line_list = line
            line_list = line_list.strip().split(COMMA)
            bug_id = int(line_list[0])
            project = line_list[3]

            if bug_id in tests_info_dict[project][UT]:
                line += ',Unit tests\n'
                file_data.append(line)

            elif bug_id in tests_info_dict[project][CT]:
                line += ',Component tests\n'
                file_data.append(line)

    with open(out_file, 'w') as out_file_handler:
        out_file_handler.write(headers)
        for line in file_data:
            out_file_handler.write(line)

    print('DONE!')


def called_methods_distribution():
    # This method calculate the distribution of methods called in each type of defects (UT/CT)

    coverage_file = join(base_dir, METRICS, COVERAGE_SUMMARY)
    coverage_list = ['bug_id,called_methods,test_type,project\n']

    tests_info_dict = read_tests_info()
    ranking_type = RAW_SPECTRUM
    for project, bugs in projects.iteritems():
        for i in xrange(1, bugs + 1):
            test_type = 'UNKNOWN'
            if i in tests_info_dict[project][UT]:
                test_type = UT
            elif i in tests_info_dict[project][CT]:
                test_type = CT

            ranking_file = join(base_dir, RANKINGS_DIR, project, str(i) + 'b', ranking_type, str(i) + RANKINGS_FILE)
            if not exists(ranking_file):
                continue

            with open(ranking_file) as _file_handler:
                called_methods = len(_file_handler.readlines())

            line = '%d,%d,%s,%s\n' % (i, called_methods, test_type, project)
            coverage_list.append(line)

    with open(coverage_file, 'w') as coverage_file_handler:
        for line in coverage_list:
            coverage_file_handler.write(line)


if __name__ == '__main__':

    # Constants
    RANKINGS_DIR = 'Rankings'
    RANKINGS_FILE = '_RankedList.csv'
    GROUND_TRUTH_DIR = 'GroundTruth'
    FAULTY_METHOD_FILE = 'faultyMethods.txt'
    TRACES_DIR = 'Traces'
    DB_FILE = 'DB.csv'
    METRICS = 'Metrics'
    SUMMARY = 'summary.csv'
    SUMMARY_TMP = 'summary_tmp.csv'
    RANK_SUMMARY = 'rank_summary.csv'
    COVERAGE_SUMMARY = 'coverage_summary.csv'
    CSV_EXTENSION = '.csv'
    RANK = 'rank'
    UT = 'UT'
    CT = 'CT'
    AVERAGE_PRECISION = 'average_precision'
    MEAN_AVERAGE_PRECISION = 'mean_average_precision'
    WASTED_EFFORT = 'wasted_effort'
    MEAN_WASTED_EFFORT = 'mean_wasted_effort'
    RAW_SPECTRUM = 'RawSpectrum'
    PATTERNED_SPECTRUM = 'PatternedSpectrum'
    METHOD_ID = 'Method_ID'
    BUG_ID = 'bug_id'
    COMMA = ','
    NEW_LINE = '\n'
    HEADER_LINE = BUG_ID + COMMA + 'acc@1,acc@3,acc@5' + COMMA + AVERAGE_PRECISION + COMMA + WASTED_EFFORT

    ranking_types = {RAW_SPECTRUM: [''], PATTERNED_SPECTRUM: ['patterns']}

    projects = {'Time': 27, 'Lang': 65, 'Closure': 133, 'Math': 106, 'Chart': 26}

    base_dir = '/Users/gulsherlaghari/datasets/dfj/Analysis'
    excluded = {}
    file_includes = ['DStar.csv', 'Barinel.csv', 'GP13.csv', 'GP19.csv', 'Naish2.csv', 'Ochiai.csv', 'Tarantula.csv', 'TarantulaStar.csv']

    test_types = [UT, CT]
    for project in projects.keys():
        excluded[project] = set()

    print('START')
    calc_metrics()
    summary_metrics()
    rank_fault_locators()
    separate_ranks_into_test_types()
    called_methods_distribution()

    print('DONE!')
