package LKManager.services;

import LKManager.model.MatchesMz.Match;
import LKManager.model.MatchesMz.Matches;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

public interface MatchService extends CrudService<Match, Long> {
    Matches findPlannedByUsername(String username) throws IOException, ParserConfigurationException, SAXException, JAXBException;

    Matches findPlayedByUsername(String username) throws IOException, ParserConfigurationException, SAXException, JAXBException;
}
