package Tasks;

import entities.Case;
import entities.Year;
import javafx.concurrent.Task;
import sample.OsdInterface;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class GetListOfPdfsTask implements Callable<Map<String, Case>> {
    private final Year year;
    OsdInterface osdInterface = new OsdInterface();

    public GetListOfPdfsTask(Year year) {
        this.year = year;
    }

    @Override
    public Map<String, Case> call() throws Exception {
        return osdInterface.getPdfsForPage(year);
    }
}