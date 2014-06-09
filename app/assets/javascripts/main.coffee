$(document).ready ->

  r = jsRoutes.controllers.G1Graphs.getAll()
  $.ajax
    url: r.url
    type: r.type
    success: (data) ->
      # TODO : handle
    error: (err) ->
      # TODO: Deal with

  nv.addGraph ->
    chart = nv.models.stackedAreaChart()
      .x( (d) ->
          d[0]
      )
      .y( (d) ->
          d[1]
      )
      .clipEdge(true)
      .useInteractiveGuideline(true)

    chart.xAxis
      .showMaxMin(false)
      .tickFormat (d) ->
        d3.time.format("%x") new Date(d)

    chart.yAxis
      .tickFormat d3.format(",.2f")

    d3.select("#G1 svg")
      .datum(data)
      .transition()
      .duration(500)
      .call chart

    nv.utils.windowResize chart.update

    chart

  return
