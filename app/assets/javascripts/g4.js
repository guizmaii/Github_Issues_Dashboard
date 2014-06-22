$(document).ready(function () {

    var r = jsRoutes.controllers.G4Controller.getAll();

    d3.json(r.url, function(data) {
        nv.addGraph(function() {
            var chart = nv.models.pieChart()
                .x(function(d) { return d.label; })
                .y(function(d) { return d.value; })
                .showLabels(true);

            d3.select("#g4 svg")
                .datum(data)
                .transition().duration(1200)
                .call(chart);

            return chart;
        });
    });


});
