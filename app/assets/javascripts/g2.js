$(document).ready(function () {

    var r = jsRoutes.controllers.G2Controller.getAll();

    d3.json(r.url, function(data) {
        nv.addGraph(function() {
            var chart = nv.models.discreteBarChart()
                .x(function(d) { return d.label; })
                .y(function(d) { return d.value; })
                .staggerLabels(false)
                .tooltips(false)
                .showValues(true)
                .showYAxis(false)
                .valueFormat(function(d) {
                   // Utilisation de moment.js et de moment-duration-format.js pour le formattage précis.
                   return moment.duration(d, 'seconds').format("M [mois], d [jours], h[h], m[min], s[s]");
                });

            d3.select('#g2 svg')
                .datum(data)
                .transition().duration(500)
                .call(chart);

            nv.utils.windowResize(chart.update);

            return chart;
        });
    });

});
