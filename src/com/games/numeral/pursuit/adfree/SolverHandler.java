package com.games.numeral.pursuit.adfree;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import com.games.common.CommonFunctions;



public final class SolverHandler extends Thread {
	public static final Logger log=Logger.getLogger("Main");

	private boolean solved=false;
	private Object lock=new Object();
	private boolean solution=true;
	private Set<GridSolver> solvers=new HashSet<GridSolver>();
	private Thread handlerThread;
	private boolean isCancelled=false;
	private final int[] grid;
	private final int place;
	private final int value;
	private CallerHandle callerHandle;

	private int[] solutionGrid;

	private boolean test=false;


	public SolverHandler(int [] grid, int place, int value, CallerHandle handle){
		this.setDaemon(true);
		this.grid=grid;
		this.place=place;
		this.value=value;
		this.callerHandle=handle;
		Blog.i("Grid in",CommonFunctions.arrayToString(grid, 9));

	}
	public void run() {
		createSolvers(grid,place,value,GridSolver.NUMBER_OF_SOLUTION_TYPES);
		handlerThread=Thread.currentThread();
		for(GridSolver s:solvers){
			s.start();
		}
		waitForSolution();

	}
	private void waitForSolution() {
		synchronized(this){
			while(true){
				try {
					if(!solved){
						this.wait();
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
					onInterrupt();
					break;
				}
				onSolved();
				break;
			}
		}
	}

	private void createSolvers(int[] grid, int place,int value,int numberOfSolvers) {
		Blog.i(numberOfSolvers);
		for(int i =0;i<numberOfSolvers;i++){
			solvers.add(new GridSolver(grid,place,value,this,"solverThread-",lock,i));
		}
	}
	private void onSolved() {
		interruptSolvers();
		if(test){
			callerHandle.onGridSolved(solutionGrid, solution, place, value);
			notifyUserSolved();
			uploadSolutionToServer();
		}
		else{
			callerHandle.onGridSolved(grid, solution, place, value);
			notifyUserSolved();
			uploadSolutionToServer();
		}

		Blog.i(Boolean.toString(solution));
		Blog.i("exiting");
	}
	private void uploadSolutionToServer() {
		if(!isCancelled){

		}
	}
	private void notifyUserSolved() {
		if(!isCancelled){

		}
	}
	private void onInterrupt() {
		interruptSolvers();
		Blog.i("interrupted");
	}
	private void interruptSolvers() {
		Blog.i("interrupting remaining solvers");
		for(GridSolver s:solvers){
			s.interrupt();
		}		
	}
	protected void setSolution(boolean solution) {
		this.solution=solution;
	}
	public synchronized void onSolverFinished(boolean result){

		if(!solved){
//			Blog.i(Thread.currentThread());
			solved=true;
			boolean solution=result;
			SolverHandler.this.setSolution(solution);
//			Blog.i(solution);
			openLock();

		}
	}
	public synchronized void onSolverFinished(boolean result, int[] solutionGrid){
		if(!solved){
			Blog.i(Thread.currentThread());
			solved=true;
			boolean solution=result;
			SolverHandler.this.setSolution(solution);
			this.solutionGrid=solutionGrid;
			Blog.i(solution);
			openLock();

		}
	}
	private void openLock() {
		this.notify();

	}
	public void cancelOperation(){
		if(handlerThread!=null){
			this.isCancelled=true;
			handlerThread.interrupt();
		}
	}
	public interface CallerHandle{
		void onGridSolved(int[] newGrid, boolean solution, int place, int value);
	}
}
