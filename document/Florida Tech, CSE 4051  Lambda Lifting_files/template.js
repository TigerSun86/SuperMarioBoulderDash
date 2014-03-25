function addRowToTable(t) {
    var tbl = document.getElementById(t);
        // Maintain the last two rows of the tabel as they are
    var lastRow = tbl.rows.length-2;
    // if there's no header row in the table, then iteration = lastRow + 1
    var iteration = lastRow;
    var row = tbl.insertRow(lastRow);
    
    // left cell
    var cellLeft = row.insertCell(0);
    var textNode = document.createTextNode('File ' + (iteration+1));
        cellLeft.setAttribute('align', "right");
        cellLeft.setAttribute('valign', "middle");
    cellLeft.appendChild(textNode);
    
    // right cell
    var cellRight = row.insertCell(1);
    var el = document.createElement('input');
    el.setAttribute('type', 'file');
    el.setAttribute('name', 'files[]');
    el.setAttribute('id', 'input file' + (iteration+1));
    el.setAttribute('size', '55');
    cellRight.appendChild(el);
}

function removeRowFromTable(t) {
    var tbl = document.getElementById(t);
        // Maintain the last two rows of the tabel as they are
    var lastRow = tbl.rows.length-2;
    if (lastRow > 1) tbl.deleteRow(lastRow - 1);
}