<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page isELIgnored="false"%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<html>
<head>
    <title>Getting Started Example</title>
    <meta charset="UTF-8">
    <!--Load the AJAX API-->
    <script type="text/javascript" src="http://www.google.com/jsapi"></script>
    <script type="text/javascript">
        //Load the Visualization API and the ready-made Google table visualization
        google.load('visualization', '1', {'packages': ['corechart']});
        // Set a callback to run when the API is loaded.
        google.setOnLoadCallback(init);
        // Send the query to the data source.
        function init() {
            // Specify the data source URL.
            var query = new google.visualization.Query('visualize?count=${count}&km=${km}');
            // Send the query with a callback function.
            query.send(handleQueryResponse);
        }
        // Handle the query response.
        function handleQueryResponse(response) {
            if (response.isError()) {
                alert('Error in query: ' + response.getMessage() + ' ' + response.getDetailedMessage());
                return;
            }

            var options = {
                title: 'Уровень воды, м',
                tooltip: {isHtml: true},
                //hAxis: {title: 'date',
                //    gridlines: {count: 15}},
                //vAxis: {title: 'level', minValue: 0, maxValue: 15},
                legend: 'none',
                width: 300, height: 160,
                curveType: "function",
                //trendlines: {
                //    0: {
                //        type: 'linear',
                //        color: 'green',
                //        lineWidth: 3,
                //        opacity: 0.3,
                //        showR2: true,
                //        visibleInLegend: true
                //    }
                //},
                series: {
                    0: {
                        0: {axis: 'days of measurement', targetAxisIndex: 0},
                        1: {axis: 'water level', targetAxisIndex: 1},
                        2: {}
                    },
                    1: {
                        0: {axis: 'days of measurement', targetAxisIndex: 0},
                        3: {axis: 'water level extrapolated', targetAxisIndex: 1},
                        4: {}
                    }
                }

                ,
                vAxes: {
                    0: {logScale: false},
                    1: {logScale: false, maxValue: 2}
                },
                hAxis: {
                    format: 'd/M/yy',
                    //gridlines: {count: 15}
                },
                //axes: {
                //    y: {
                //        'hours studied': {label: 'Hours Studied'},
                //        'final grade': {label: 'Final Exam Grade'}
                //    }
                //       }
            };

            // Draw the visualization.
            var data = response.getDataTable();
            //dirty hack
            //data.Ff[2].p.p={html: true};
            //data.Ff[2].p.role="tooltip";
            //data.Ff[2].p={html: true};
            //data.Ff[2].role="tooltip";
            data.setColumnProperties(2, {
                role: 'tooltip',
                html: true
            });

            data.setColumnProperties(4, {
                role: 'tooltip',
                html: true
            });

            var chart = new google.visualization.ScatterChart(document.getElementById('chart_div'));
            chart.draw(data, options);

        }
    </script>
</head>
<body>
<h1>График уровня Волги по водомерным постам</h1>
Выберите интересующий вас водомерный пост и число точек на графике:
<form id="kms" action="/welcome">
<select name="km" onchange="submit()">
    <option value="908" ${km==908  ? 'selected' : ''}>Нижний Новгород</option>
    <option value="1303" ${km==1303  ? 'selected' : ''}>Казань</option>
    <option value="1665" ${km==1665  ? 'selected' : ''}>Тольятти</option>
    <c:forEach items="${streamgauges}" var="value">
        <option value="${value[1]}" ${value[1]==km  ? 'selected' : ''}>${value[0]}</option>
    </c:forEach>

</select>
    <select name="count" onchange="submit()">
        <option value="10" ${count==10  ? 'selected' : ''}>10</option>
        <option value="20" ${count==20  ? 'selected' : ''}>20</option>
        <option value="30" ${count==30  ? 'selected' : ''}>30</option>
        <option value="50" ${count==50  ? 'selected' : ''}>50</option>
        <option value="-1" ${count==-1  ? 'selected' : ''}>Все</option>
    </select>
</form>

<!--Div that will hold the visualization-->
<div id="chart_div"></div>

<textarea style="width: 300px;height: 200px;" class="select-on-click" id="embed" readonly="">&lt;iframe height='200' width='300' frameborder='0' allowtransparency='true' scrolling='no' src='http://volgalevel.appspot.com/iframe?km=${km}&count=${count}'&gt;&lt;/iframe&gt;</textarea>

<c:forEach items="${allfiles}" var="value">
<br>
    <a href="/file?date=${value.date}">${value.name} (${value.datevisible})</a>
</c:forEach>

</body>
</html>