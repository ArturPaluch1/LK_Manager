package LKManager.controllers.LK;

import LKManager.LK.Terminarz;
import LKManager.model.UserMZ.UserData;
import LKManager.services.LKUserService;
import LKManager.services.MZUserService;
import LKManager.services.PlikiService;
import LKManager.services.TerminarzService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.xml.sax.SAXException;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller

public class terminarzController {
    private final MZUserService MZUserService;
    private final LKUserService lkUserService;
    private final TerminarzService terminarzService;
    private final PlikiService plikiService;
    private Integer numerRundy;
    private String wybranyTerminarz;
    private Terminarz terminarz;
    public terminarzController(MZUserService MZUserService, LKUserService lkUserService, TerminarzService terminarzService, PlikiService plikiService) {
        this.MZUserService = MZUserService;

        this.lkUserService = lkUserService;

        this.terminarzService = terminarzService;
        this.plikiService = plikiService;
    }

    public Integer getNumerRundy() {
        return numerRundy;
    }

    public void setNumerRundy(Integer numerRundy) {
        this.numerRundy = numerRundy;
    }

    @GetMapping("/terminarz")
    public String index(Model model, @RequestParam(value = "wybranyTerminarz", required = false) String wybranyTerminarz) throws ParserConfigurationException, IOException, SAXException, JAXBException, DatatypeConfigurationException, ParseException, URISyntaxException {


        var terminarze = plikiService.pobierzPlikiZFolderu(PlikiService.folder.terminarze);

        if (wybranyTerminarz != null) {
            //sprawdzanie czy jest taki terminarz

            terminarz = terminarzService.wczytajTerminarz(wybranyTerminarz);
            if (terminarz == null) {
//todo przekierowanie że nie ma takiego terminarza
                return "redirect:/temp";
            }

            //nastapila zmiana terminarza ->nr rundy=1
            if (wybranyTerminarz != this.wybranyTerminarz) {
                this.wybranyTerminarz = wybranyTerminarz;
                numerRundy = 1;
            }


        }


        //nie przekazano terminarza
        if (this.wybranyTerminarz == null) {
            // nie ma terminarzy -> przekierowanie do tworzenia
            if (terminarze.length == 0) {

                return "redirect:/dodajTerminarz";
            } else {
                //wybieranie najnowszego moyfikowanego
                var najbardziejNiedawnoZmodyfikowanyTerminarz = Arrays.stream(terminarze).toList().stream().max(Comparator.comparing(File::lastModified));
                terminarz = terminarzService.wczytajTerminarz(najbardziejNiedawnoZmodyfikowanyTerminarz.get().getName());
                model.addAttribute("wybranyTerminarz", najbardziejNiedawnoZmodyfikowanyTerminarz.get().getName());
            }
        }
        //wskazano terminarz
        else {
            //wybrany terminarz
            terminarz = terminarzService.wczytajTerminarz(this.wybranyTerminarz);
            model.addAttribute("wybranyTerminarz", this.wybranyTerminarz);
        }


        model.addAttribute("nrRundy", terminarz.getTerminarz());
        if (numerRundy == null) {
            numerRundy = 1;
        }


        model.addAttribute("runda", terminarz.getTerminarz().get(numerRundy - 1));
        model.addAttribute("mecze", terminarz.getTerminarz().get(numerRundy - 1).getMecze()
        );
        model.addAttribute("numerRundy", numerRundy);


        model.addAttribute("terminarze", terminarze);


        return "LK/terminarz/terminarz";
    }


    @RequestMapping("/dodajTerminarz")
    public String dodajTerminarz(Model model) throws JAXBException, IOException, ParserConfigurationException, SAXException {

        var gracze = lkUserService.wczytajGraczyZXML();
        gracze = gracze.stream().sorted(
                (o1, o2) -> o1.getUsername().compareToIgnoreCase(o2.getUsername())
        ).collect(Collectors.toList());




        var terminarzCommand = new TerminarzCommand();

        model.addAttribute("gracze", gracze);





        terminarzCommand.lista = new ArrayList<String>();

        //terminarzCommand.bob=b;
        model.addAttribute("userListWrapper", terminarzCommand);

        model.addAttribute("terminarz", terminarzCommand);
        return "LK/terminarz/dodajTerminarz";
    }


    @GetMapping(value = "/terminarz/wybierz")
    public String wybierzTerminarz() {
        return "LK/terminarz/wybierzTerminarz";
    }


    @GetMapping("/usunTerminarz")
    public String usuwanieTerminarza(Model model)//@RequestParam (value = "wybranyTerminarz", required = true)String terminarzDoUsuniecia)
    {

        var terminarze = plikiService.pobierzPlikiZFolderu(PlikiService.folder.terminarze);
//this.wybranyTerminarz=null;
        model.addAttribute("terminarze", terminarze);
        return "LK/terminarz/usunTerminarz";


    }

    @PostMapping("/usunTerminarz")
    public String usunTerminarz(@RequestParam(value = "wybranyTerminarz", required = true) String terminarzDoUsuniecia) {
        try {
            File plikDoUsuniecia = new File("Data/terminarze/" + terminarzDoUsuniecia);
            plikDoUsuniecia.delete();
            return "redirect:/terminarz";
        } catch (Exception e) {
            return "LK/temp";
        }

    }

    @PostMapping("/terminarz")
    public String stworzTerminarz(Model model, @ModelAttribute @Valid TerminarzCommand command, @RequestParam(value = "wybraniGracze", required = false) List<String> wybraniGracze) throws DatatypeConfigurationException, JsonProcessingException {
//todo walidacja  te same nazwy

        XMLGregorianCalendar data = DatatypeFactory.newInstance().newXMLGregorianCalendar();

        data.setYear(Integer.parseInt(command.data.trim().split("-")[0]));
        data.setMonth(Integer.parseInt(command.data.split("-")[1]));
        data.setDay(Integer.parseInt(command.data.split("-")[2]));

////////////////////////////////////////


        var gracze = lkUserService.wczytajGraczyZXML();


        //nie wybrano graczy do wielodniowego terminarza
        if (wybraniGracze == null) {
            if (command.getLista().size() == 0) {
                //todo nie wybrano tez pojedynczych meczy
                int tt = 0;
            } else {
                if (command.getLista().size() % 2 != 0) {
                    //todo brakuje pary dla grajka  ewentuanie w js wybierzgrajka-> pauza
                } else {
                    List<UserData> templist = new ArrayList<>();
                    for (int i = 0; i < command.lista.size(); i++
                    ) {


                        for (int j = 0; j < gracze.size(); j++) {


                            if (gracze.get(j).getUsername().equals(command.lista.get(i))) {
                                templist.add(gracze.get(j));
                                j = 0;
                                break;
                            }
                        }
                    }
//////////////////////////////
                    terminarzService.utworzTerminarzJednodniowy(data, templist, command.nazwa);
                }

            }
        } else {
            List<UserData> templist = new ArrayList<>();
            for (var gracz : gracze
            ) {
                for (int i = 0; i < wybraniGracze.size(); i++
                ) {
                    if (gracz.getUsername().equals(wybraniGracze.get(i))) {
                        templist.add(gracz);
                        i = 0;
                        break;
                    }
                }
            }
//////////////////////////////
            terminarzService.utworzTerminarzWielodniowy(data, templist, command.nazwa);
        }


        return "redirect:/terminarz";
    }

    @PostMapping("/pokazRunde")
    public String pokazRunde(@RequestParam(value = "participant", required = true) Integer numerRundy) {
        int oo = 9;
        this.numerRundy = numerRundy;
        return "redirect:/terminarz";

    }


    @Getter
    @Setter
    @NoArgsConstructor
    public class TerminarzCommand {
/*todo walidacja
https://blog.mloza.pl/java-bean-validation-spring-boot-sprawdzanie-poprawnosci-danych-w-spring-boocie/

*/

        @NotNull
        private String data;
        @NotBlank
        private String nazwa;
        private List<String> lista;


        public void setListaGraczy() {

        }


    }



}
