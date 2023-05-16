package pers.winter.threadpool;

/**
 * The handler for thread pool executors.
 * Before put tasks into the executor, you must create an handler to deal with the tasks.
 * @param <T> What kind of tasks will be executed.
 */
public interface IExecutorHandler<T> {
    /**
     * The thread pool will call execute when deal with a task
     * @param task Current task.
     */
    public void execute(T task) throws Exception;

    /**
     * If thread pool gets an exception while calling {@link IExecutorHandler#execute(Object)}, it will call this method to deal with the exception.
     * @param task The task causing exception
     * @param cause The exception
     */
    public void exceptionCaught(T task, Throwable cause);
}
