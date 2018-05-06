rm(list=ls())
library("effsize")

raw_baseDir = '/Users/gulsherlaghari/datasets/dfj/Analysis/Metrics/RawSpectrum'
pat_baseDir = '/Users/gulsherlaghari/datasets/dfj/Analysis/Metrics/PatternedSpectrum/patterns'
result_file = '/Users/gulsherlaghari/datasets/gulsher_paper_evaluating_spectrum_analyses/significance_tests.txt'

projects <- c('Closure', 'Math', 'Lang', 'Time', 'Chart')
pat_top_fault_locators <- c('Ochiai', 'Ochiai', 'GP13', 'Ochiai', 'Barinel')
raw_top_fault_locators <- c('GP13', 'DStar', 'GP13', 'GP13', 'Tarantula')

global_raw = ''
global_pat = ''

read.files <- function(project, fault_locator_raw, fault_locator_pat) {
  fault_locator_raw <- paste(fault_locator_raw, 'csv', sep = '.')
  fault_locator_pat <- paste(fault_locator_pat, 'csv', sep = '.')
  
  local_raw <- read.csv(paste(raw_baseDir, project, fault_locator_raw, sep='/'))
  local_pat <- read.csv(paste(pat_baseDir, project, fault_locator_pat, sep='/'))
  eval.parent(substitute(global_raw <- local_raw))
  eval.parent(substitute(global_pat <- local_pat))
}


print.test.result <- function(project, raw, pat, pat_vs_raw = TRUE) {
  if(pat_vs_raw) {
    ap_test_result <- wilcox.test(pat$average_precision, raw$average_precision, paired = TRUE, alternative = 'greater')
    we_test_result <- wilcox.test(pat$wasted_effort, raw$wasted_effort, paired = TRUE, alternative = 'less')
    p.value <- format(ap_test_result$p.value, scientific = TRUE)
    if(ap_test_result$p.value < 0.05) {
      significant <- 'Significant'
      p.value <- paste('\\textbf{', p.value, '}', sep = '')
    }
    else
      significant <- 'Non-Significant'
    write(x=paste('Patterned > Raw   [', project, ' ]    AP = ', p.value, significant, sep=' '), file=result_file, append=TRUE)
    treatment <- pat$average_precision
    control <- raw$average_precision
    effect_size <- cliff.delta(treatment,control,return.dm=TRUE)
    estimate <- paste('\\magnitude', effect_size$magnitude, '{', round(effect_size$estimate,1), '}', sep='')
    write(x=paste('Patterned > Raw   [', project, ' ]    Cliff Delta (AP) = ', estimate, sep=' '), file=result_file, append=TRUE)
    
    p.value <- format(we_test_result$p.value, scientific = TRUE)
    if(ap_test_result$p.value < 0.05) {
      significant <- 'Significant'
      p.value <- paste('\\textbf{', p.value, '}', sep = '')
    }
    else
      significant <- 'Non-Significant'
    write(x=paste('Patterned > Raw   [', project, ']     WE = ', p.value, significant, sep=' '), file=result_file, append=TRUE)
    treatment <- pat$wasted_effort
    control <- raw$wasted_effort
    effect_size <- cliff.delta(treatment,control,return.dm=TRUE)
    estimate <- paste('\\magnitude', effect_size$magnitude, '{', round(effect_size$estimate,1), '}', sep='')
    write(x=paste('Patterned > Raw   [', project, ' ]    Cliff Delta (WE) = ', estimate, sep=' '), file=result_file, append=TRUE)
  }
  else {
    ap_test_result <- wilcox.test(raw$average_precision, pat$average_precision, paired = TRUE, alternative = 'greater')
    we_test_result <- wilcox.test(pat$wasted_effort, raw$wasted_effort, paired = TRUE, alternative = 'less')
    p.value <- format(ap_test_result$p.value, scientific = TRUE)
    if(ap_test_result$p.value < 0.05) {
      significant <- 'Significant'
      p.value <- paste('\\textbf{', p.value, '}', sep = '')
    }
    else
      significant <- 'Non-Significant'
    write(x=paste('Raw > Patterned   [', project, ' ]    AP = ', p.value, significant, sep=' '), file=result_file, append=TRUE)
    
    treatment <- raw$average_precision
    control <- pat$average_precision
    effect_size <- cliff.delta(treatment,control,return.dm=TRUE)
    estimate <- paste('\\magnitude', effect_size$magnitude, '{', round(effect_size$estimate,1), '}', sep='')
    write(x=paste('Raw > Patterned   [', project, ' ]    Cliff Delta (AP) = ', estimate, sep=' '), file=result_file, append=TRUE)
    
    
    p.value <- format(we_test_result$p.value, scientific = TRUE)
    if(ap_test_result$p.value < 0.05) {
      significant <- 'Significant'
      p.value <- paste('\\textbf{', p.value, '}', sep = '')
    }
    else
      significant <- 'Non-Significant'
    write(x=paste('Raw > Patterned   [', project, ']     WE = ', p.value, significant, sep=' '), file=result_file, append=TRUE)
    
    treatment <- raw$wasted_effort
    control <- pat$wasted_effort
    effect_size <- cliff.delta(treatment,control,return.dm=TRUE)
    estimate <- paste('\\magnitude', effect_size$magnitude, '{', round(effect_size$estimate,1), '}', sep='')
    write(x=paste('Raw > Patterned   [', project, ' ]    Cliff Delta (WE) = ', estimate, sep=' '), file=result_file, append=TRUE)
  }
  
  write(x='-------------------------------------------------------\n', file=result_file, append=TRUE)
}


# Per project defects
write(x='Per projects tests', file=result_file)
write(x='-------------------------------------------------------\n', file=result_file, append=TRUE)

raw_all = NULL
pat_all = NULL

for(i in 1:length(projects)) {
  project <- projects[i]
  fault_locator_raw <- raw_top_fault_locators[i]
  fault_locator_pat <- pat_top_fault_locators[i]
  read.files(project = project, fault_locator_raw = fault_locator_raw, fault_locator_pat = fault_locator_pat)
  if(is.null(raw_all)) {
    raw_all <- global_raw
  }
  else {
    raw_all <- rbind(raw_all, global_raw)
  }
  
  if(is.null(pat_all)) {
    pat_all <- global_pat
  }
  else{
    pat_all <- rbind(pat_all, global_pat)
  }
  
  if(project == 'Chart') {
    print.test.result(project = project, raw = global_raw, pat = global_pat, pat_vs_raw = FALSE)
  }
  else {
    print.test.result(project = project, raw = global_raw, pat = global_pat)
  }
}

# All defects together

write(x='All defects together', file=result_file, append = TRUE)
write(x='-------------------------------------------------------\n', file=result_file, append=TRUE)

print.test.result(project = 'Overall', raw = raw_all, pat = pat_all)


#   For unit tests
# _____________________________________________________________________

raw_baseDir = '/Users/gulsherlaghari/datasets/dfj/Analysis/Metrics/RawSpectrum'
pat_baseDir = '/Users/gulsherlaghari/datasets/dfj/Analysis/Metrics/PatternedSpectrum/patterns'

projects <- c('Math', 'Lang')
pat_top_fault_locators <- c('Ochiai', 'GP19')
raw_top_fault_locators <- c('DStar', 'GP19', 'DStar')

global_raw = ''
global_pat = ''

# Per project defects
write(x='\n\nUnit tests', file=result_file, append=TRUE)
write(x='Per projects tests', file=result_file, append=TRUE)
write(x='-------------------------------------------------------\n', file=result_file, append=TRUE)

raw_all = NULL
pat_all = NULL

for(i in 1:length(projects)) {
  project <- projects[i]
  fault_locator_raw <- raw_top_fault_locators[i]
  fault_locator_pat <- pat_top_fault_locators[i]
  read.files(project = paste(project, 'UT', sep = '/'), fault_locator_raw = fault_locator_raw, fault_locator_pat = fault_locator_pat)
  if(is.null(raw_all)) {
    raw_all <- global_raw
  }
  else {
    raw_all <- rbind(raw_all, global_raw)
  }
  
  if(is.null(pat_all)) {
    pat_all <- global_pat
  }
  else{
    pat_all <- rbind(pat_all, global_pat)
  }
  
  # if(project == 'Math' || project == 'Chart') {
  if(project %in% c('Math', 'Chart')) {
    print.test.result(project = project, raw = global_raw, pat = global_pat, pat_vs_raw = FALSE)
  }
  else {
    print.test.result(project = project, raw = global_raw, pat = global_pat)
  }
}

# All defects together

write(x='All defects together', file=result_file, append = TRUE)
write(x='-------------------------------------------------------\n', file=result_file, append=TRUE)

print.test.result(project = 'Overall', raw = raw_all, pat = pat_all, pat_vs_raw = FALSE)


#   For component tests
# _____________________________________________________________________

raw_baseDir = '/Users/gulsherlaghari/datasets/dfj/Analysis/Metrics/RawSpectrum'
pat_baseDir = '/Users/gulsherlaghari/datasets/dfj/Analysis/Metrics/PatternedSpectrum/patterns'

projects <- c('Closure', 'Math', 'Lang', 'Time', 'Chart')
pat_top_fault_locators <- c('Ochiai', 'DStar', 'GP19', 'Ochiai', 'Tarantula')
raw_top_fault_locators <- c('GP13', 'DStar', 'Naish2', 'GP13', 'Tarantula')

global_raw = ''
global_pat = ''

# Per project defects
write(x='\n\nComponent tests', file=result_file, append=TRUE)
write(x='Per projects tests', file=result_file, append=TRUE)
write(x='-------------------------------------------------------\n', file=result_file, append=TRUE)

raw_all = NULL
pat_all = NULL

for(i in 1:length(projects)) {
  project <- projects[i]
  fault_locator_raw <- raw_top_fault_locators[i]
  fault_locator_pat <- pat_top_fault_locators[i]
  read.files(project = paste(project, 'CT', sep = '/'), fault_locator_raw = fault_locator_raw, fault_locator_pat = fault_locator_pat)
  if(is.null(raw_all)) {
    raw_all <- global_raw
  }
  else {
    raw_all <- rbind(raw_all, global_raw)
  }
  
  if(is.null(pat_all)) {
    pat_all <- global_pat
  }
  else{
    pat_all <- rbind(pat_all, global_pat)
  }
  print.test.result(project = project, raw = global_raw, pat = global_pat)
}

# All defects together

write(x='All defects together', file=result_file, append = TRUE)
write(x='-------------------------------------------------------\n', file=result_file, append=TRUE)

print.test.result(project = 'Overall', raw = raw_all, pat = pat_all)


write(x='\n\n\nCalled methods info and tests', file=result_file, append = TRUE)
write(x='-------------------------------------------------------\n', file=result_file, append=TRUE)

methods_distro <- read.csv('/Users/gulsherlaghari/datasets/dfj/Analysis/Test_Types/methods_count_in_tests.csv')
methods_distro_CT <- methods_distro[methods_distro$Test_Type == 'CT',]
methods_distro_UT <- methods_distro[methods_distro$Test_Type == 'UT',]
test_result <- t.test(methods_distro_CT$Called_Methods, methods_distro_UT$Called_Methods, alternative = 'greater')
num_UT <- paste('# unit tests', length(methods_distro_UT$Called_Methods))
num_CT <- paste('# component tests', length(methods_distro_CT$Called_Methods))

methods_UT <- summary(methods_distro_UT$Called_Methods)
methods_UT <- paste('MEAN = ', round(x=mean(methods_UT), 2), 'SD = ', round(x=sd(methods_UT), 2))

methods_CT <- summary(methods_distro_CT$Called_Methods)
methods_CT <- paste('MEAN = ', round(x=mean(methods_CT), 2), 'SD = ', round(x=sd(methods_CT), 2))

write(x=num_UT, file=result_file, append = TRUE)
write(x=methods_UT, file=result_file, append = TRUE)
write(x=num_CT, file=result_file, append = TRUE)
write(x=methods_CT, file=result_file, append = TRUE)
p.value <- format(test_result$p.value, scientific = TRUE)
write(x=paste('Difference between means > 0 - significance ', p.value), file=result_file, append = TRUE)
treatment <- methods_distro_CT$Called_Methods
control <- methods_distro_UT$Called_Methods
effect_size <- cohen.d(treatment,control)
estimate <- paste('\\magnitude', effect_size$magnitude, '{', round(effect_size$estimate,1), '}', sep='')
write(x=paste('Cohen\'s d = ', estimate, sep=' '), file=result_file, append=TRUE)

t.test(methods_distro_CT$Called_Methods, methods_distro_UT$Called_Methods, alternative = 'greater')
effect_size
