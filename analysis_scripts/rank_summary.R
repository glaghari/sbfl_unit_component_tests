
rm(list=ls())
library(ggplot2)

font_size = 12
font_family = 'Times'
defects_category = 'Defects Category'

baseDir <- '/Users/gulsherlaghari/datasets/dfj/Analysis/Metrics'
# This is for all summary
filename_ALL <- paste(baseDir, 'rank_summary_test_types', sep='/')
p.data_ALL <- read.csv(paste(filename_ALL, '.csv', sep=''))
levels(p.data_ALL$analysis) = c('Basic Family', 'Extended Family')

levels(p.data_ALL$fault_locator)[levels(p.data_ALL$fault_locator) == 'DStar'] <- "D*"
levels(p.data_ALL$fault_locator)[levels(p.data_ALL$fault_locator) == 'TarantulaStar'] <- "T*"

# This is for all summary
g_ALL <- ggplot(p.data_ALL, aes(x=rank, color=fault_locator))
g_ALL <- g_ALL + stat_density(geom="line",position="identity", adjust=5)
g_ALL <- g_ALL + facet_grid(analysis~project)
g_ALL <- g_ALL + theme_bw()
g_ALL <- g_ALL + scale_x_log10(name = 'Absolute rank (log scale)', breaks = c(1,10,100,1000,10000))
g_ALL <- g_ALL + scale_y_continuous(name = "Density")
g_ALL <- g_ALL + theme(legend.position = 'top', legend.key.width = unit(1,'cm'), legend.key = element_rect(colour= 'transparent'), plot.margin=grid::unit(c(-5.5,0,0,0), 'mm'), text=element_text(size=font_size, family=font_family))
g_ALL <- g_ALL + guides(colour = guide_legend(nrow = 1, title = 'FL Technique: ', override.aes=list(size=3)))

width = 12
height = 6

ggsave(paste(baseDir, 'rank_summary.pdf', sep = '/'), g_ALL, width = width, height = height)
