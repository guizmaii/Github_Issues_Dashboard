$(document).ready(function () {

    var r = jsRoutes.controllers.G1Controller.getAll();

    d3.json(r.url, function(data) {
        nv.addGraph(function() {
            var chart = nv.models.stackedAreaChart()
                .margin({right: 100})
                .x(function(d) { return d[0]; })   //We can modify the data accessor functions...
                .y(function(d) { return d[1]; })   //...in case your data is formatted differently.
                .useInteractiveGuideline(true)    //Tooltips which show all data points. Very nice!
                .transitionDuration(500)
                .showControls(true)       //Allow user to choose 'Stacked', 'Stream', 'Expanded' mode.
                .clipEdge(true);

            //Format x-axis labels with custom function.
            chart.xAxis
                .tickFormat(function(d) {
                    return d3.time.format('%d-%m-%Y')(new Date(d));
                });

            chart.yAxis
                .tickFormat(d3.format('d'));

            d3.select('#g1 svg')
                .datum(data)
                .call(chart);

            nv.utils.windowResize(chart.update);

            return chart;
        });
    });

});
