$(document).ready(function () {

    var r = jsRoutes.controllers.G3Controller.getAll();

    d3.json(r.url, function(data) {
        nv.addGraph(function() {
            var chart = nv.models.multiBarChart();

            chart.yAxis
                .tickFormat(d3.format(',.1f'));

            chart.multibar.stacked(true); // default to stacked

            d3.select('#g3 svg')
                .datum(data())
                .transition().duration(500)
                .call(chart);

            nv.utils.windowResize(chart.update);

            return chart;
        });
    });

});
