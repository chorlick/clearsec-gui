package Tasks;

import entities.Year;
import sample.OsdInterface;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class GetListOfYearsTask implements Callable<Map<Integer, Year>> {
    OsdInterface osdInterface = new OsdInterface();

    public Map<Integer, Year> call() throws Exception {
        return osdInterface.getYears();
    }
}