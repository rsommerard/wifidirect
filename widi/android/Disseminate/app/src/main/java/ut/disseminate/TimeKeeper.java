package ut.disseminate;

/**
 * Created by Venkat on 12/2/14.
 */
public class TimeKeeper
{
    private long elapsedTime;
    private long startTime;
    private boolean isRunning;
    private long endTime;
    public static final double NANOS_PER_SEC = 1000000000.0;

    public TimeKeeper()
    {  reset();
    }


    public void start()
    {  if (isRunning) return;
        isRunning = true;
        startTime = System.nanoTime();
    }


    public void stop()
    {  if (!isRunning) return;
        isRunning = false;
        endTime = System.nanoTime();
        elapsedTime = elapsedTime + endTime - startTime;
    }


    public long checkTime()
    {
        if (isRunning)
        {
            long newEndTime = System.nanoTime ();
            elapsedTime = elapsedTime + newEndTime - startTime;
            startTime = endTime;
        }
        return elapsedTime;
    }

    public long getStartTime(){
        return startTime;
    }
    public long getEndTime(){
        return endTime;
    }

    public void reset()
    {  elapsedTime = 0;
        isRunning = false;
    }
}