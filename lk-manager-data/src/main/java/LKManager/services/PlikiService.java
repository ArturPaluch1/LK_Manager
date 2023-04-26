package LKManager.services;

import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class PlikiService {

    public File[] pobierzPlikiZFolderu(folder wybranyFolder) {
        File folder = new File("Data/" + wybranyFolder);
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                pobierzPlikiZFolderu(wybranyFolder);
            } else {
                System.out.println(fileEntry.getName());
            }
        }
        return folder.listFiles();
    }

    public enum folder {
        terminarze,
        gracze
    }
}
