
rm(list=ls())
library(ggplot2)

font_size = 12
font_family = 'Times'
defects_category = 'Failing Test Category'

baseDir <- '/Users/gulsherlaghari/datasets/dfj/Analysis/Test_Types'


filename <- 'methods_count_in_tests'

filename <- paste(baseDir, filename, sep='/')
p.data <- read.csv(paste(filename, '.csv', sep=''))

g <- ggplot(p.data, aes(y=Called_Methods, x=Test_Type, group=Test_Type))
# g <- g + geom_boxplot(aes(colour=Test_Type))
g <- g + geom_violin(aes(fill=factor(Test_Type), colour=factor(Test_Type)), alpha=0.1, show.legend = FALSE)
g <- g + geom_boxplot(aes(colour=Test_Type), width=.1)
# g <- g + geom_jitter(size=0.01, position = 'jitter')
# g <- g + facet_grid(.~Project)
g <- g + theme_bw()
g <- g + scale_colour_manual(labels = c('Component tests', 'Unit tests'), values = c('red', 'blue'))
g <- g + scale_y_log10(name = '# methods executed (log scale)', breaks = c(1,10,100,1000,10000))
g <- g + scale_x_discrete(name = defects_category, labels=c('UT' = 'Unit tests', 'CT' = 'Component tests'))
# g <- g + scale_y_continuous(name = "Density")
g <- g + theme(legend.position = 'top',
               legend.key.width = unit(1,'cm'),
               legend.key = element_rect(colour= 'transparent'),
               plot.margin=grid::unit(c(-5.5,0,0,0), 'mm'),
               text=element_text(size=font_size, family=font_family)
)
g <- g + guides(colour = guide_legend(nrow = 1, title.hjust = 0.5, title = paste(defects_category, ':')))
width = 6
height = 3
ggsave(paste(filename, '.pdf', sep=''), g, width = width, height = height)
