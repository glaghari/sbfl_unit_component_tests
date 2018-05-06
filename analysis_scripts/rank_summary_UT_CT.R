
rm(list=ls())
library(ggplot2)

font_size = 25
font_family = 'Times'
defects_category = 'Defects Category'

baseDir <- '/Users/gulsherlaghari/datasets/dfj/Analysis/Metrics'
filename <- paste(baseDir, 'rank_summary_test_types', sep='/')
p.data <- read.csv(paste(filename, '.csv', sep=''))
levels(p.data$analysis) = c('Basic Family', 'Extended Family')


levels(p.data$fault_locator)[levels(p.data$fault_locator) == 'DStar'] <- 'D*'
levels(p.data$fault_locator)[levels(p.data$fault_locator) == 'TarantulaStar'] <- 'T*'

g_UT_CT <- ggplot(p.data, aes(x=rank, color=fault_locator))
g_UT_CT <- g_UT_CT + stat_density(geom="line", position="identity", adjust=5)

# g_UT_CT <- g_UT_CT + facet_grid(fault_type~analysis)
g_UT_CT <- g_UT_CT + facet_grid(analysis~fault_type)

g_UT_CT <- g_UT_CT + theme_bw()
g_UT_CT <- g_UT_CT + scale_x_log10(name = 'Absolute rank (log scale)', breaks = c(1,10,100,1000,10000))
g_UT_CT <- g_UT_CT + scale_y_continuous(name = "Density")
 
g_UT_CT <- g_UT_CT + scale_linetype_discrete(name = paste(defects_category, ':'))
# g_UT_CT <- g_UT_CT + scale_colour_discrete(name = 'Fault Locator: ')

g_UT_CT <- g_UT_CT + theme(legend.position = 'top', legend.key.width = unit(0.7,'cm'), legend.box.just = 'left', legend.box = 'horizontal', legend.key = element_rect(colour= 'transparent'), plot.margin=grid::unit(c(-4.5,0,1,0), 'mm'), text=element_text(size=font_size, family=font_family))
g_UT_CT <- g_UT_CT + guides(colour = guide_legend(nrow = 1, title.hjust = 0.5, title = 'FL Technique: ', override.aes=list(size=3)))

width = 12
height = 12

ggsave(paste(baseDir, 'rank_summary_UT_CT.pdf', sep='/'), g_UT_CT, width = width, height = height)

