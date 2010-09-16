package nom.tam.fits;

import nom.tam.util.Cursor;

/** This class allows FITS binary and ASCII tables to
 *  be accessed via a common interface.
 * 
 *  Bug Fix: 3/28/01 to findColumn.
 */
public abstract class TableHDU extends BasicHDU {

  private TableData table;

  TableHDU(TableData td) {
    table = td;
  }

  public Object[] getRow(int row) throws FitsException {
    return table.getRow(row);
  }

  public Object getColumn(String colName) throws FitsException {
    return getColumn(findColumn(colName));
  }

  public Object getColumn(int col) throws FitsException {
    return table.getColumn(col);
  }

  public Object getElement(int row, int col) throws FitsException {
    return table.getElement(row, col);
  }

  public void setRow(int row, Object[] newRow) throws FitsException {
    table.setRow(row, newRow);
  }

  public void setColumn(String colName, Object newCol) throws FitsException {
    setColumn(findColumn(colName), newCol);
  }

  public void setColumn(int col, Object newCol) throws FitsException {
    table.setColumn(col, newCol);
  }

  public void setElement(int row, int col, Object element) throws FitsException {
    table.setElement(row, col, element);
  }

  public int addRow(Object[] newRow) throws FitsException {

    int row = table.addRow(newRow);
    myHeader.addValue("NAXIS2", row, "number of rows in table");
    return row;
  }

  public final int findColumn(final String colName) {

    String val;
    for (int i = 0, len = getNCols(); i < len; i++) {
      // LAURENT : use getTrimmedStringValue instead of getStringValue :
      val = myHeader.getTrimmedStringValue("TTYPE" + (i + 1));
      if (val != null && val.equals(colName)) {
        return i;
      }
    }
    return -1;
  }

  public abstract int addColumn(Object data) throws FitsException;

  /** Get the number of columns for this table
   * @return The number of columns in the table.
   */
  public int getNCols() {
    return table.getNCols();
  }

  /** Get the number of rows for this table
   * @return The number of rows in the table.
   */
  public int getNRows() {
    return table.getNRows();
  }

  /**
   * Get the name of a column in the table.
   * @param index The 0-based column index.
   * @return The column name or null if an invalid index was requested.
   */
  public final String getColumnName(final int index) {
    // LAURENT : use getTrimmedStringValue instead of getStringValue :
    return myHeader.getTrimmedStringValue("TTYPE" + (index + 1));
  }

  /**
   * Set the name of a column in the table.
   *
   * @param index The 0-based column index.
   * @param name column name
   * @param comment description of the column
   * @throws FitsException
   */
  public void setColumnName(final int index, final String name, final String comment)
          throws FitsException {
    setColumnName(index, name, comment, null);
  }

  /**
   * Set the name and unit of a column in the table.
   *
   * // LAURENT : added to specify the unit of the column
   *
   * @param index The 0-based column index.
   * @param name column name
   * @param comment description of the column
   * @param unit column unit
   * @throws FitsException
   */
  public void setColumnName(final int index, final String name, final String comment, final String unit)
          throws FitsException {
    if (getNCols() > index && index >= 0) {
      final Cursor iter = myHeader.positionAfterIndex("TFORM", index + 1);
      // Insert TTYPE keyword before TFORM
      iter.prev();
      myHeader.addValue("TTYPE" + (index + 1), name, comment);
      // Set cursor position after TFORM
      iter.next();
      if (unit != null && unit.length() > 0) {
        // Insert TUNIT keyword after TFORM
        myHeader.addValue("TUNIT" + (index + 1), unit, "physical unit of field " + (index + 1));
      }
    }
  }

  /**
   * Get the FITS type of a column in the table.
   *
   * // LAURENT : removed FitsException and index boundary checks
   *
   * @param index The 0-based column index.
   * @return The FITS type or null if an invalid index was requested.
   */
  public final String getColumnFormat(final int index) {
    // LAURENT : use getTrimmedStringValue instead of getStringValue :
    return myHeader.getTrimmedStringValue("TFORM" + (index + 1));
  }

  public void setCurrentColumn(int col) {
    myHeader.positionAfterIndex("TFORM", (col + 1));
  }

  /**
   * Get the unit of a column in the table.
   *
   * // LAURENT : added method
   *
   * @param index The 0-based column index.
   * @return The unit of a column or null if undefined or an invalid index was requested.
   */
  public final String getColumnUnit(final int index) {
    return myHeader.getTrimmedStringValue("TUNIT" + (index + 1));
  }

  /**
   * Remove all rows from the table starting at some specific index from the table.
   * Inspired by a routine by R. Mathar but re-implemented using the DataTable and
   * changes to AsciiTable so that it can be done easily for both Binary and ASCII tables.
   * @param row the (0-based) index of the first row to be deleted.
   * @throws FitsExcpetion if an error occurs.
   */
  public void deleteRows(final int row) throws FitsException {
    deleteRows(row, getNRows() - row);
  }

  /**
   * Remove a number of adjacent rows from the table.  This routine
   * was inspired by code by R.Mathar but re-implemented using changes
   * in the ColumnTable class abd AsciiTable so that we can do
   * it for all FITS tables.
   * @param firstRow the (0-based) index of the first row to be deleted.
   *	This is zero-based indexing: 0<=firstrow< number of rows.
   * @param nRow the total number of rows to be deleted.
   * @throws FitsException  If an error occurs in the deletion.
   */
  public void deleteRows(final int firstRow, int nRow) throws FitsException {

    // Just ignore invalid requests.
    if (nRow <= 0 || firstRow >= getNRows() || nRow <= 0) {
      return;
    }

    /* correct if more rows are requested than available */
    if (nRow > getNRows() - firstRow) {
      nRow = getNRows() - firstRow;
    }

    table.deleteRows(firstRow, nRow);
    myHeader.setNaxis(2, getNRows(), "number of rows in table");
  }

  /** Delete a set of columns from a table.
   */
  public void deleteColumnsIndexOne(int column, int len) throws FitsException {
    deleteColumnsIndexZero(column - 1, len);
  }

  /** Delete a set of columns from a table.
   */
  public void deleteColumnsIndexZero(int column, int len) throws FitsException {
    deleteColumnsIndexZero(column, len, columnKeyStems());
  }

  /** Delete a set of columns from a table.
   *  @param column The one-indexed start column.
   *  @param len    The number of columns to delete.
   *  @param fields Stems for the header fields to be removed
   *                for the table.
   */
  public void deleteColumnsIndexOne(int column, int len, String[] fields) throws FitsException {
    deleteColumnsIndexZero(column - 1, len, fields);
  }

  /** Delete a set of columns from a table.
   *  @param column The zero-indexed start column.
   *  @param len    The number of columns to delete.
   *  @param fields Stems for the header fields to be removed
   *                for the table.
   */
  public void deleteColumnsIndexZero(int column, int len, String[] fields) throws FitsException {

    if (column < 0 || len < 0 || column + len > getNCols()) {
      throw new FitsException("Illegal columns deletion request- Start:" + column + " Len:" + len + " from table with " + getNCols() + " columns");
    }

    if (len == 0) {
      return;
    }

    int ncol = getNCols();
    table.deleteColumns(column, len);


    // Get rid of the keywords for the deleted columns
    for (int col = column; col < column + len; col++) {
      for (int fld = 0; fld < fields.length; fld ++) {
        String key = fields[fld] + (col + 1);
        myHeader.deleteKey(key);
      }
    }

    // Shift the keywords for the columns after the deleted columns
    for (int col = column + len; col < ncol; col ++) {
      for (int fld = 0; fld < fields.length; fld ++) {
        String oldKey = fields[fld] + (col + 1);
        String newKey = fields[fld] + (col + 1 - len);
        if (myHeader.containsKey(oldKey)) {
          myHeader.replaceKey(oldKey, newKey);
        }
      }
    }
    // Update the number of fields.
    myHeader.addValue("TFIELDS", getNCols(), "number of fields in each row");

    // Give the data sections a chance to update the header too.
    table.updateAfterDelete(ncol, myHeader);
  }

  /** Get the stems of the keywords that are associated
   *  with table columns.  Users can supplement this
   *  with their own and call the appropriate deleteColumns fields.
   */
  public abstract String[] columnKeyStems();
}
