package com.games.numeral.pursuit.adfree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.games.common.CommonFunctions;

public class GridSolver extends Thread {
	private static final int SUBS[][]={{0,1,2},{3,4,5},{6,7,8}};
	private static final int TYPE_BUSIEST_ROW = 0;
	private static final int TYPE_BUSIEST_COLUMN=1;
	private static final int TYPE_FIRST_TO_LAST=2;
	private static final int TYPE_LAST_TO_FIRST=3;
	private int[] solutionGrid=null;
	private int[] newGrid=new int[9*9];
	private boolean wasSolution;
	private int solutionType;
	private Rows rows;
	private Columns cols;
	public static int NUMBER_OF_SOLUTION_TYPES=3;
	private Subgrids subgrids;
	private int newPlace;
	private int newValue;
	private SolverHandler manager;
	private boolean test=false;
	public GridSolver(int[] grid, int newPlace, int newValue, SolverHandler manager, String threadName, Object lock, int solutionType){
		super(threadName+Integer.toString(solutionType));
		this.setDaemon(true);
		System.arraycopy(grid, 0, this.newGrid, 0, grid.length);
		if(newGrid[newPlace]!=0){
			throw new IllegalArgumentException("Attempted to place value in a square that was already occupied");
		}
		if(newValue>9 || newValue<1){
			throw new IllegalArgumentException("Illegal value for placement, value can not be " + Integer.toString(newValue));
		}
		this.solutionType=solutionType;
		this.newPlace=newPlace;
		this.newValue=newValue;
		this.manager=manager;
	}

	@Override
	public void run(){
		initSolver();
		Cell candidateCell=new Cell().setPlace(newPlace).setValue(newValue);
		if(candidateCell.isValid()){
			candidateCell.setOccupied();
			Cell cellOne=setupCells(this.solutionType);
			Blog.i(cellOne.stringAll());
//			Long time=System.currentTimeMillis();
			this.wasSolution=isSolution(cellOne);
			//			Blog.i("Time elapsed: ",System.currentTimeMillis()-time);

		}
		else{
			Blog.i("not valid");
			this.wasSolution=false;
		}
		if(test){
			this.manager.onSolverFinished(wasSolution,this.solutionGrid);
		}
		else{
			this.manager.onSolverFinished(wasSolution);
		}

	}


	private void initSolver() {
		Cell occupiedFirst=getOccupiedCells();
		this.rows=new Rows(occupiedFirst);
		this.cols=new Columns(occupiedFirst);
		this.subgrids=new Subgrids(occupiedFirst);		
	}
	/**Checks if there is any solution to a sudoku puzzle. 
	 * 
	 * @param cellOne The first cell of a doubly linked list of all empty cells in the sudoku puzzle in arbitrary order.
	 * @return if there is a solution or not
	 */
	private boolean isSolution(Cell cellOne) {
		Cell c=cellOne;
		while(true){
			if(c.isValid()){
				c.setOccupied();
				if(c.isLast()){
					solutionGrid=newGrid;
					Blog.i("Solution found");
					Blog.i(CommonFunctions.arrayToString(newGrid, 9));
					return true;
				}
				c=c.getNext();
			}
			else if(!c.isMax()){
				c.setNextValid();
			}
			else{
				while(c.isMax()&&!c.isFirst()){
					c.setValue(0);
					c=c.getPrev();
					c.setUnOccupied();
				}
				if(c.isFirst()&& c.isMax()){
					solutionGrid=newGrid;
					Blog.i("No solution");
					Blog.i(CommonFunctions.arrayToString(newGrid, 9));
					return false;
				}
				c.setNextValid();
			}
			if(Thread.interrupted()){
				return false;
			}
//			Blog.i(CommonFunctions.arrayToString(newGrid, 9));
		} 
	}
	public Cell setupCells(int solutionType){
		switch(solutionType){		
		case(TYPE_BUSIEST_ROW):
			return setupCellsBusiestRowFirst();
		case(TYPE_BUSIEST_COLUMN):
			return setupCellsBusiestColumnFirst();
		case(TYPE_FIRST_TO_LAST):
			return setupCellsFirstToLast();
		case(TYPE_LAST_TO_FIRST):
			return setupCellsLastToFirst();
		}
		return randomOrder();
	}
	private Cell getOccupiedCells() {
		Cell firstCell=null;
		Cell cell=null;
		int i=0;
		while(i<newGrid.length){
			if(firstCell==null && newGrid[i]!=0){
				firstCell=new Cell().setPlace(i).setValue(newGrid[i]);
				cell=firstCell;
				continue;
			}
			if(newGrid[i]!=0){
				Cell next=new Cell().setPlace(i).setValue(newGrid[i]);
				next.setPrev(cell);
				cell.setNext(next);
				cell=next;
			}
			i++;
		}
		return firstCell;
	}

	private Cell setupCellsBusiestColumnFirst() {
		int busy=cols.getBusiestColumn();
		Blog.i(busy);
		int begin=cols.getFirstCellInColumn(busy);
		return cols.getCellsFromBusiestColumn(begin);
	}
	private Cell setupCellsBusiestRowFirst() {
		int busy=rows.getBusiestRow();
		int begin=rows.getFirstCellInRow(busy);
		return rows.getCellsFromBusiestRow(begin);
	}
	private Cell randomOrder() {
		ArrayList<Cell> cells=new ArrayList<Cell>();
		for(int i=0;i<newGrid.length;i++){
			if(newGrid[i]==0){
				cells.add(new Cell().setPlace(i).setValue(0));
			}
		}
		Collections.shuffle(cells);
		for(int i=1;i<cells.size();i++){
			cells.get(i-1).setNext(cells.get(i));
			cells.get(i).setPrev(cells.get(i-1));
		}
		return cells.get(0);

	}
	private Cell setupCellsLastToFirst() {
		Cell cell=null;
		Cell firstCell=null;
		for(int i=newGrid.length-1;i>=0;i--){
			if(newGrid[i]==0 && firstCell==null){
				firstCell=new Cell().setPlace(i).setValue(0);
				cell=firstCell;
				continue;
			}
			if(newGrid[i]==0){
				Cell nextCell=new Cell().setPlace(i).setValue(0).setPrev(cell);
				cell.setNext(nextCell);
				cell=nextCell;
			}
		}

		return firstCell;
	}
	private Cell setupCellsFirstToLast() {
		Cell cell=null;
		Cell firstCell=null;
		for(int i=0;i<newGrid.length;i++){
			if(newGrid[i]==0 && firstCell==null){
				firstCell=new Cell().setPlace(i).setValue(0);
				cell=firstCell;
				continue;
			}
			if(newGrid[i]==0){
				Cell nextCell=new Cell().setPlace(i).setValue(0).setPrev(cell);
				cell.setNext(nextCell);
				cell=nextCell;
			}
		}
		return firstCell;
	}
	public boolean wasFoundToBeSolution(){
		return wasSolution;
	}
	private class Cell{
		private static final int MAX_VALUE=9;
		private int value;
		private int place;
		private Cell prev;
		private Cell next;
		/**If run on first Cell, returns a string representation of all cells with a count*/
		public String stringAll() {
			StringBuilder s=new StringBuilder();
			Cell c=this;
			int amount=1;
			while(c.hasNext()){
				s.append(c.toString());
				s.append(", Nr: " +Integer.toString(amount));
				s.append("\n");
				amount++;
				c=c.getNext();
			};
			s.append(c.toString());
			return s.toString();
		}
		private Cell setValue(int value){
			this.value=value;
			GridSolver.this.newGrid[place]=value;
			return this;
		}
		public void setUnOccupied() {
			rows.remove(this);
			cols.remove(this);
			subgrids.remove(this);
		}
		public Cell setOccupied() {
			if(rows!=null){
				rows.addCell(this);
				cols.addCell(this);
				subgrids.addCell(this);
			}
			return this;
		}
		public boolean isLast() {
			return this.next==null;
		}
		public boolean isFirst() {
			return this.prev==null;
		}
		public Cell setNextValid() {
			this.setValue(value+1);
			while(!this.isValid()&&!isMax()){
				this.setValue(value+1);
			}
			return this;
		}
		public boolean isMax() {
			return this.value>=MAX_VALUE;
		}
		public Cell setPrev(Cell cell) {
			this.prev=cell;
			return this;
		}
		public Cell setNext(Cell cell){
			this.next=cell;
			return this;
		}
		private Cell setPlace(int place){
			this.place=place;
			return this;
		}
		private Cell getNext(){
			return this.next;
		}
		private Cell getPrev(){
			return this.prev;
		}
		private boolean isValid(){
			boolean valid=true;
			if(this.value==0){
				valid=false;
			}
			if(rows.hasValue(this)){
				return false;
			}
			if(cols.hasValue(this)){
				return false;
			}
			if(subgrids.hasValue(this)){
				return false;
			}
			return valid;
		}
		private boolean hasNext(){
			return this.next!=null;
		}
		@Override
		public boolean equals(Object o){
			Cell compareTo;
			try{
				compareTo=(Cell)o;
			}catch(Exception e){
				return false;
			}
			if(compareTo.value==this.value){
				return true;
			}
			else{
				return false;
			}
		}
		@Override
		public int hashCode(){
			return this.value;
		}
		@Override
		public String toString(){
			return "Cell has"+"Place: "+Integer.toString(place)+", Value: "+Integer.toString(value);
		}
		public int getRow() {
			return place/9;
		}
		public int getColumn() {
			return place-9*(place/9);
		}
		public Integer getValue() {
			return value;
		}
		public int getSubgrid() {
			return SUBS[getRow()/3][getColumn()/3];
		}
	}
	private class Rows{
		private ArrayList<Set<Integer>> rows=new ArrayList<Set<Integer>>(9);
		public Rows(Cell occupiedFirst){
			for(int i=0;i<9;i++){
				rows.add(new HashSet<Integer>());
			}
			if(occupiedFirst!=null){
				fillOccupiedCells(occupiedFirst);
			}
		}
		public Cell getCellsFromBusiestRow(int begin) {
			int i=begin;
			Cell firstCell=null,cell=null;
			while(i<newGrid.length){
				if(newGrid[i]==0&&firstCell==null){
					firstCell=new Cell().setPlace(i).setValue(newGrid[i]);
					cell=firstCell;
				}
				else if(newGrid[i]==0){
					Cell nextCell=new Cell().setPlace(i).setValue(0).setPrev(cell);
					cell.setNext(nextCell);
					cell=nextCell;
				}
				i++;
			}
			i=begin-1;
			while(i>-1){
				if(newGrid[i]==0){
					if(firstCell==null){
						firstCell=new Cell().setPlace(i).setValue(newGrid[i]);
						cell=firstCell;
					}
					Cell nextCell=new Cell().setPlace(i).setValue(0).setPrev(cell);
					cell.setNext(nextCell);
					cell=nextCell;
				}
				i--;
			}
			return firstCell;
		}
		public int getFirstCellInRow(int busy) {
			return (busy)*9;
		}
		public int getBusiestRow() {
			int max=0,r=0;
			for(int i=0;i<rows.size();i++){
				if(rows.get(i).size()>max){
					max=rows.get(i).size();
					r=i;
				}
			}
			return r;
		}
		public void remove(Cell cell) {
			rows.get(cell.getRow()).remove(cell.getValue());
		}
		private Rows fillOccupiedCells(Cell firstCell){
			Cell cell=firstCell;
			while(cell.hasNext()){
				this.addCell(cell);
				cell=cell.getNext();
			}
			this.addCell(cell);

			return this;
		}
		public Rows addCell(Cell cell) {
			rows.get(cell.getRow()).add(cell.getValue());
			return this;
		}
		public boolean hasValue(Cell cell){
			if(rows.get(cell.getRow()).contains(cell.getValue())){
				return true;
			}
			else{
				return false;
			}
		}
		@Override
		public String toString(){
			return this.rows.toString();
		}
	}
	private class Columns{

		private ArrayList<Set<Integer>> columns=new ArrayList<Set<Integer>>(9);
		public Columns(Cell occupiedFirst){
			for(int i=0;i<9;i++){
				columns.add(new HashSet<Integer>());
			}
			if(occupiedFirst!=null){
				fillOccupiedCells(occupiedFirst);
			}
		}
		public Cell getCellsFromBusiestColumn(int begin) {
			int i=begin;
			Cell firstCell=null,cell=null;
			boolean countUp=true;
			while(true){
				if(newGrid[i]==0&&firstCell==null){
					firstCell=new Cell().setPlace(i).setValue(newGrid[i]);
					cell=firstCell;
				}
				else if(newGrid[i]==0){
					Cell nextCell=new Cell().setPlace(i).setValue(0).setPrev(cell);
					cell.setNext(nextCell);
					cell=nextCell;
				}
				i=nextIndexInCol(i,countUp);
				if(i==-1){
					break;
				}
			}
			countUp=false;
			i=begin-1;
			while(i>-1){
				if(newGrid[i]==0&&firstCell==null){
					firstCell=new Cell().setPlace(i).setValue(newGrid[i]);
					cell=firstCell;
				}
				else if(newGrid[i]==0){
					Cell nextCell=new Cell().setPlace(i).setValue(0).setPrev(cell);
					cell.setNext(nextCell);
					cell=nextCell;
				}
				i=nextIndexInCol(i,countUp);
				if(i==-1){
					break;
				}
			}
			return firstCell;
		}
		private int nextIndexInCol(int i,boolean countUp) {
			if(hasNextIndexInCol(i)){
				return i+9;
			}
			else if(countUp && i<newGrid.length-1){
				return i-9*(i/9)+1;
			}
			else if(!countUp){
				int newI=i-9*(i/9)-1;
				if(newI>-1){
					return newI;
				}
				else{
					return -1;
				}
			}
			else{
				return -1;
			}
		}
		private boolean hasNextIndexInCol(int i) {
			return i+9<newGrid.length;
		}
		public int getFirstCellInColumn(int busy) {
			return busy;
		}
		public int getBusiestColumn() {
			int max=0,r=0;
			for(int i=0;i<columns.size();i++){
				if(columns.get(i).size()>max){
					max=columns.get(i).size();
					r=i;
				}
			}
			return r;
		}
		public void remove(Cell cell) {
			columns.get(cell.getColumn()).remove(cell.getValue());

		}
		private Columns fillOccupiedCells(Cell firstCell){
			Cell cell=firstCell;
			while(cell.hasNext()){
				this.addCell(cell);
				cell=cell.getNext();
			}
			this.addCell(cell);
			return this;
		}
		public Columns addCell(Cell cell) {
			columns.get(cell.getColumn()).add(cell.getValue());
			return this;
		}
		public boolean hasValue(Cell cell){
			if(columns.get(cell.getColumn()).contains(cell.getValue())){
				return true;
			}
			else{
				return false;
			}
		}
		@Override
		public String toString(){
			return this.columns.toString();
		}

	}
	private class Subgrids{

		private ArrayList<Set<Integer>> subs=new ArrayList<Set<Integer>>(9);
		public Subgrids(Cell occupiedFirst){
			for(int i=0;i<9;i++){
				subs.add(new HashSet<Integer>());
			}
			if(occupiedFirst!=null){
				fillOccupiedCells(occupiedFirst);
			}
		}
		public void remove(Cell cell) {
			subs.get(cell.getSubgrid()).remove(cell.getValue());

		}
		private Subgrids fillOccupiedCells(Cell firstCell){
			Cell cell=firstCell;
			while(cell.hasNext()){
				this.addCell(cell);
				cell=cell.getNext();
			}
			this.addCell(cell);

			return this;
		}
		public Subgrids addCell(Cell cell) {
			subs.get(cell.getSubgrid()).add(cell.getValue());
			return this;
		}
		public boolean hasValue(Cell cell){
			if(subs.get(cell.getSubgrid()).contains(cell.getValue())){
				return true;
			}
			else{
				return false;
			}
		}
		@Override
		public String toString(){
			return this.subs.toString();
		}

	}
}
