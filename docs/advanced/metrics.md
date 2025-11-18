# Metrics

Flight provides integration with bStats for plugin analytics.

## Overview

Metrics allow you to:

- **Track plugin usage** - See how many servers use your plugin
- **Custom charts** - Add custom data visualization
- **Version tracking** - Track plugin versions in use

## Basic Usage

Override methods in `FlightPlugin`:

```java
@Override
protected int getBStatsId() {
    return YOUR_BSTATS_ID; // Get from bStats website
}

@Override
protected List<Metrics.CustomChart> getCustomMetricCharts() {
    List<Metrics.CustomChart> charts = new ArrayList<>();

    // Add custom charts
    charts.add(new Metrics.SimplePie("feature_usage", () -> {
        return "enabled";
    }));

    return charts;
}
```

## Custom Charts

Flight supports various chart types:

- **SimplePie** - Pie chart with single value
- **AdvancedPie** - Pie chart with multiple values
- **SimpleBarChart** - Bar chart
- **AdvancedBarChart** - Advanced bar chart
- **MultiLineChart** - Multi-line chart
- **SingleLineChart** - Single line chart
- **DrilldownPie** - Drilldown pie chart

## See Also

- [Advanced Features](../advanced.md) - Other advanced features
