class Clock {

    private int _hour, _minute, _second;
    
    private List<SecondChangeHandler> listeners = new ArrayList<SecondChangeHandler>();

    public void run() throws InterruptedException {
        
        while (true) {
            
            Thread.sleep(1000);
            
            Calendar now = Calendar.getInstance();
            int hour = now.get(Calendar.HOUR_OF_DAY);
            int minute = now.get(Calendar.MINUTE);
            int second = now.get(Calendar.SECOND);
            
            if (second != _second) {
                TimeInfoEventArgs args = new TimeInfoEventArgs(hour, minute, second);
                onSecondChange(this, args);
            }
            
            // update the time
            _hour = hour;
            _minute = minute;
            _second = second;
        }
    }
    
    public void register(SecondChangeHandler listener) {
        this.listeners.add(listener);
    }
    
    protected void onSecondChange(Object clock, TimeInfoEventArgs args) {
        for (SecondChangeHandler listener : listeners) {
            listener.handle(clock, args);
        }
    }
    
    public static final class TimeInfoEventArgs {
        private int hour, minute, second;

        public TimeInfoEventArgs(int hour, int minute, int second) {
            this.hour = hour;
            this.minute = minute;
            this.second = second;
        }

        public int getHour() {
            return hour;
        }

        public int getMinute() {
            return minute;
        }

        public int getSecond() {
            return second;
        }
    }
}


interface SecondChangeHandler {
    public void handle(Object sender, TimeInfoEventArgs args);
}

class DisplayClock implements SecondChangeHandler {

    @Override
    public void handle(Object sender, TimeInfoEventArgs args) {
        System.out.println(String.format("Current Time: %d:%d:%d", args.getHour(), args.getMinute(), args.getSecond()));
    }

}

class LogClock implements SecondChangeHandler {

    @Override
    public void handle(Object sender, TimeInfoEventArgs args) {
        System.out.println(String.format("Logging to file: %d:%d:%d", args.getHour(), args.getMinute(), args.getSecond()));
    }

}

public class DelegationTest {
    public static void main(String[] args) throws InterruptedException {
        Clock c = new Clock();
        SecondChangeHandler dh = new DisplayClock();
        SecondChangeHandler lh = new LogClock();
        c.register(dh);
        c.register(lh);
        
        c.run();
    }
}

