<%--

<p>
    Number of Publications with NO parseable "Publication Year" => 6
    <br />
    {2004=2, 2005=2, 2006=8, DNA=6}
</p>

<style type="text/css">
	.sparkline_style table{
		margin: 0;
   		padding: 0;
   		width: auto;
   		border-collapse: collapse;
    	border-spacing: 0;
    	vertical-align: inherit;

	}
</style>

<script type="text/javascript">
    function drawVisualization() {
        var data = new google.visualization.DataTable();
        data.addColumn('string', 'Year');
        data.addColumn('number', 'Publications');
        data.addRows(7);
        data.setValue(0, 0, '2004');
        data.setValue(0, 1, 2);
        data.setValue(1, 0, '2005');
        data.setValue(1, 1, 2);
        data.setValue(2, 0, '2006');
        data.setValue(2, 1, 8);
        data.setValue(3, 0, '2007');
        data.setValue(3, 1, 0);
        data.setValue(4, 0, '2008');
        data.setValue(4, 1, 0);
        data.setValue(5, 0, '2009');
        data.setValue(5, 1, 0);
        data.setValue(6, 0, '2010');
        data.setValue(6, 1, 0);
        var fullSparklineView = new google.visualization.DataView(data);
        var shortSparklineView = new google.visualization.DataView(data);
        shortSparklineView.setColumns([1]);
        fullSparklineView.setColumns([1]);
        shortSparklineView.setRows(data.getFilteredRows([{
            column: 0,
            minValue: '2001',
            maxValue: '2010'}]));
        var full_spark = new google.visualization.ImageSparkLine(document.getElementById('pub_count_full_sparkline_viz'));
        full_spark.draw(fullSparklineView, {
            width: 63,
            height: 21,
            showAxisLines: false,
            showValueLabels: false,
            labelPosition: 'none'
        });
        var short_spark = new google.visualization.ImageSparkLine(document.getElementById('pub_count_short_sparkline_viz'));
        short_spark.draw(shortSparklineView, {
            width: 63,
            height: 21,
            showAxisLines: false,
            showValueLabels: false,
            labelPosition: 'none'
        });
        var shortSparkRows = shortSparklineView.getViewRows();
        var renderedShortSparks = 0;
        $.each(shortSparkRows, function(index, value) {
            renderedShortSparks += data.getValue(value, 1);
        });
        var shortSparksText = '<p>' + renderedShortSparks + ' Papers with year from ' + ' 18 ' + ' total' + '</p>';
        $(shortSparksText).prependTo('#pub_count_short_sparkline_viz');
        var allSparksText = '<p><b>Full Timeline</b> ' + 12 + ' Papers with year from ' + ' 18 ' + ' total' + '</p>';
        $(allSparksText).prependTo('#pub_count_full_sparkline_viz');
    }
    $(document).ready(function() {
        if ($('#pub_count_short_sparkline_viz').length == 0) {
            $('<div/>', {
                'id': 'pub_count_short_sparkline_viz',
                'class': 'sparkline_style'

            }).appendTo('#ajax_recipient');
        }
        if ($('#pub_count_full_sparkline_viz').length == 0) {
            $('<div/>', {
                'id': 'pub_count_full_sparkline_viz',
                'class': 'sparkline_style'
            }).appendTo('#ajax_recipient');
        }
        drawVisualization();
    });
</script>

--%>

${requestScope.visContentCode}

${requestScope.visContextCode}
