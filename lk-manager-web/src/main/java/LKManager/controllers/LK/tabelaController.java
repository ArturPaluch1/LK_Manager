package LKManager.controllers.LK;

import LKManager.LK.Comparators.GraczPodsumowanieComparatorGoalDifference;
import LKManager.LK.Comparators.GraczPodsumowanieComparatorGoalLost;
import LKManager.LK.Comparators.GraczPodsumowanieComparatorGoalScored;
import LKManager.LK.Comparators.GraczPodsumowanieComparatorPoints;
import LKManager.LK.GraczPodsumowanie;
import LKManager.LK.Tabela;
import LKManager.LK.Terminarz;
import LKManager.services.LKUserService;
import LKManager.services.MZUserService;
import LKManager.services.PlikiService;
import LKManager.services.TerminarzService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Controller
//@RequestMapping({"tabela.html", "tabela", "tabela"} )
public class tabelaController {
    private final MZUserService MZUserService;

    private final TerminarzService terminarzService;
    private final PlikiService plikiService;
    private final LKUserService lkUserService;
    private String wybranyTerminarz;
    private Terminarz terminarz;

    public tabelaController(MZUserService MZUserService, TerminarzService terminarzService, PlikiService plikiService, LKUserService lkUserService) {
        this.MZUserService = MZUserService;

        this.terminarzService = terminarzService;


        this.plikiService = plikiService;
        this.lkUserService = lkUserService;
    }


    @GetMapping({"tabela.html", "tabela", "tabela"})
    public String index(Model model, @RequestParam(value = "wybranyTerminarz", required = false) String wybranyTerminarz) throws URISyntaxException, IOException, JAXBException {


        if (wybranyTerminarz != null) {
            //nastapila zmiana terminarza ->nr rundy=1
            if (wybranyTerminarz != this.wybranyTerminarz) {
                this.wybranyTerminarz = wybranyTerminarz;

            }


        }


        var terminarze = plikiService.pobierzPlikiZFolderu(PlikiService.folder.terminarze);


        Tabela tabela = new Tabela();


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


        for (var item : lkUserService.wczytajGraczyZXML()
        ) {

            for (var gracz : terminarz.getTerminarz().get(0).getMecze()
            ) {
                if (gracz.getUser().getUsername().equals(item.getUsername())
                        || gracz.getopponentUser().getUsername().equals(item.getUsername())) {
                    var tempGracz = new GraczPodsumowanie();
                    tempGracz.setGracz(item);

                    tabela.getGraczePodsumowanie().add(tempGracz);
                }

            }


        }

////////////////////////////////////////////////////////////////////
        for (var runda : terminarz.getTerminarz()
        ) {
            for (var mecz : runda.getMecze()
            ) {

                if (LocalDate.now().plusDays(1).isAfter(LocalDate.parse(runda.getData().toString()))) {
                    var meczUser = mecz.getUser();
                    var meczOpponent = mecz.getopponentUser();


                    var user = tabela.getGraczePodsumowanie().stream().
                            filter(a -> a.getGracz().getUserId().equals(mecz.getUser().getUserId())).findFirst().orElse(null);
                    var userOp = tabela.getGraczePodsumowanie().stream().
                            filter(a -> a.getGracz().getUserId().equals(mecz.getopponentUser().getUserId())).findFirst().orElse(null);


                    if (userOp != null && userOp.getGracz().getTeamlist().get(0).getTeamName() != "pauza") {
                        var goleStrzelone1 = mecz.getUserMatchResult1();
                        var goleStrzelone2 = mecz.getUserMatchResult2();
                        var goleStracone1 = mecz.getOpponentMatchResult1();
                        var goleStracone2 = mecz.getOpponentMatchResult2();

                        if (goleStrzelone1 == "" || goleStrzelone2 == "" || goleStracone1 == "" || goleStracone2 == "") {
// nic nie rob ( Skoro nie ma wyników, to znaczy, że team id z terminarza nie równa się team id z rozegranego meczu,
// czyli zaplanowany mecz w terminarzu nie odbył się)
// czyli obaj mają po 0 punktów (bez zniam)
                        } else {
                            var sumaPunktow = user.getSumaPunktow();
                            var userGoleStrzelone = user.getGoleStrzelone();
                            var userGoleStracone = user.getGoleStracone();

                            if (sumaPunktow == "") sumaPunktow = "0";
                            if (userGoleStrzelone == "") userGoleStrzelone = "0";
                            if (userGoleStracone == "") userGoleStracone = "0";


                            user.setGoleStrzelone(
                                    String.valueOf(Integer.parseInt(userGoleStrzelone)
                                            + Integer.parseInt(goleStrzelone1)
                                            + Integer.parseInt(goleStrzelone2))
                            );
                            user.setGoleStracone(
                                    String.valueOf(Integer.parseInt(userGoleStracone)
                                            + Integer.parseInt(goleStracone1)
                                            + Integer.parseInt(goleStracone2))
                            );

                            userGoleStrzelone = user.getGoleStrzelone();
                            userGoleStracone = user.getGoleStracone();

                            user.setRoznica(Integer.parseInt(userGoleStrzelone) - Integer.parseInt(userGoleStracone));


                            if (Integer.parseInt(goleStrzelone1) > Integer.parseInt(goleStracone1)) {
                                user.setSumaPunktow(String.valueOf(Integer.parseInt(sumaPunktow) + 3));
                            } else if (Integer.parseInt(goleStrzelone1) == Integer.parseInt(goleStracone1)) {
                                user.setSumaPunktow(String.valueOf(Integer.parseInt(sumaPunktow) + 1));
                            }

                            sumaPunktow = user.getSumaPunktow();
                            if (sumaPunktow == "") sumaPunktow = "0";

                            if (Integer.parseInt(goleStrzelone2) > Integer.parseInt(goleStracone2)) {
                                user.setSumaPunktow(String.valueOf(Integer.parseInt(sumaPunktow) + 3));
                            } else if (Integer.parseInt(goleStrzelone2) == Integer.parseInt(goleStracone2)) {
                                user.setSumaPunktow(String.valueOf(Integer.parseInt(sumaPunktow) + 1));
                            }


                            /////////////////////////////////////////////////////


                            var OpponentgoleStrzelone1 = mecz.getOpponentMatchResult1();
                            var OpponentgoleStrzelone2 = mecz.getOpponentMatchResult2();
                            var OpponentgoleStracone1 = mecz.getUserMatchResult1();
                            var OpponentgoleStracone2 = mecz.getUserMatchResult2();

                            if (OpponentgoleStrzelone1 == "") OpponentgoleStrzelone1 = "0";
                            if (OpponentgoleStrzelone2 == "") OpponentgoleStrzelone2 = "0";
                            if (OpponentgoleStracone1 == "") OpponentgoleStracone1 = "0";
                            if (OpponentgoleStracone2 == "") OpponentgoleStracone2 = "0";

                            var OpponentsumaPunktow = userOp.getSumaPunktow();
                            var OpponentGoleStrzelone = userOp.getGoleStrzelone();
                            var OpponentGoleStracone = userOp.getGoleStracone();

                            if (OpponentsumaPunktow == "") OpponentsumaPunktow = "0";
                            if (OpponentGoleStrzelone == "") OpponentGoleStrzelone = "0";
                            if (OpponentGoleStracone == "") OpponentGoleStracone = "0";


                            userOp.setGoleStrzelone(
                                    String.valueOf(Integer.parseInt(OpponentGoleStrzelone)
                                            + Integer.parseInt(OpponentgoleStrzelone1)
                                            + Integer.parseInt(OpponentgoleStrzelone2))

                            );
                            userOp.setGoleStracone(
                                    String.valueOf(Integer.parseInt(OpponentGoleStracone)
                                            + Integer.parseInt(OpponentgoleStracone1)
                                            + Integer.parseInt(OpponentgoleStracone2))
                            );
                            OpponentGoleStrzelone = userOp.getGoleStrzelone();
                            OpponentGoleStracone = userOp.getGoleStracone();

                            userOp.setRoznica(Integer.parseInt(OpponentGoleStrzelone) - Integer.parseInt(OpponentGoleStracone));

                            if (Integer.parseInt(OpponentgoleStrzelone1) > Integer.parseInt(OpponentgoleStracone1)) {
                                userOp.setSumaPunktow(String.valueOf(Integer.parseInt(OpponentsumaPunktow) + 3));
                            } else if (Integer.parseInt(OpponentgoleStrzelone1) == Integer.parseInt(OpponentgoleStracone1)) {
                                userOp.setSumaPunktow(String.valueOf(Integer.parseInt(OpponentsumaPunktow) + 1));
                            }

                            OpponentsumaPunktow = userOp.getSumaPunktow();
                            if (OpponentsumaPunktow == "") OpponentsumaPunktow = "0";

                            if (Integer.parseInt(OpponentgoleStrzelone2) > Integer.parseInt(OpponentgoleStracone2)) {
                                userOp.setSumaPunktow(String.valueOf(Integer.parseInt(OpponentsumaPunktow) + 3));
                            } else if (Integer.parseInt(OpponentgoleStrzelone2) == Integer.parseInt(OpponentgoleStracone2)) {
                                userOp.setSumaPunktow(String.valueOf(Integer.parseInt(OpponentsumaPunktow) + 1));
                            }

                        }
                    }
//}
                }

            }

        }
///////////////////////////////////////////////
        //sortowanie po punktach
        tabela.getGraczePodsumowanie().sort(new GraczPodsumowanieComparatorPoints());
        var gracze = tabela.getGraczePodsumowanie();
        boolean nowaRownosc = false;

//indeksy zmian punktów -> i=początek zmiany
        List<Integer> listaIndeksow = new ArrayList<>();
        listaIndeksow.add(0);
        for (int i = 0; i < gracze.size() - 1; i++) {
            if (!(gracze.get(i + 1).getSumaPunktow().equals(gracze.get(i).getSumaPunktow()))) {
                listaIndeksow.add(i + 1);
            }

        }

        // sprawdzanie czy punkty są równe i sortowanie po różnicy
        List<GraczPodsumowanie> tempstring = new ArrayList<>();
        for (int i = 0; i < listaIndeksow.size(); i++) {
            //sortowanie końcówki listy (ze względu na porównanie ostatniego indeksu z i+1)
            if (i == listaIndeksow.size() - 1) {
                //  var  posortowane=  gracze.subList(listaIndeksow.get(listaIndeksow.size()-1),gracze.size());
                var posortowane = gracze.subList(listaIndeksow.get(i), (gracze.size()));
                posortowane.sort(new GraczPodsumowanieComparatorGoalDifference());

                sortujRoznicaBramek(tempstring, posortowane);


                //sortowanie po strzelonych
                //  posortowane.sort(new GraczPodsumowanieComparatorGoalScored());

            }
            //sortowanie normalnie
            else {
                var posortowane = gracze.subList(listaIndeksow.get(i), listaIndeksow.get(i + 1));
                posortowane.sort(new GraczPodsumowanieComparatorGoalDifference());

                sortujRoznicaBramek(tempstring, posortowane);


                //sortowanie po strzelonych
                //   posortowane.sort(new GraczPodsumowanieComparatorGoalScored());
                //  tempstring.addAll(posortowane);
                //sortowanie posortowane  po straconych
            }


        }
//sortowanie pozostalych


        //tempstring= posortowane przez pkt i roznice


        tabela.getGraczePodsumowanie().clear();

        tabela.setGraczePodsumowanie(tempstring);

        //// sprawdzanie czy roznica jest rowna i sortowanie po strzelonych


        ///////////////////////////////


        model.addAttribute("terminarze", terminarze);

        model.addAttribute("tabela", tabela.getGraczePodsumowanie());

        return "LK/tabela";
    }

    protected void sortujRoznicaBramek(List<GraczPodsumowanie> tempstring, List<GraczPodsumowanie> posortowane) {
        var listaIndeksowPowtorzonychRoznic = znajdzIndeksyPowtorzonychRoznic(posortowane);

        for (int j = 0; j < listaIndeksowPowtorzonychRoznic.size(); j++) {
            //sortowanie końcówki listy (ze względu na porównanie ostatniego indeksu z i+1)
            if (j == listaIndeksowPowtorzonychRoznic.size() - 1) {


                var posortowaneRoznice = posortowane.subList(listaIndeksowPowtorzonychRoznic.get(j), (posortowane.size()));
                // sortowanie po strzelonych
                posortowaneRoznice.sort(new GraczPodsumowanieComparatorGoalScored());

                sortujStracone(tempstring, posortowaneRoznice);
                //  tempstring.addAll(posortowaneRoznice);
            } else {
                var posortowaneRoznice = posortowane.subList(listaIndeksowPowtorzonychRoznic.get(j), listaIndeksowPowtorzonychRoznic.get(j + 1));
                posortowaneRoznice.sort(new GraczPodsumowanieComparatorGoalScored());

                sortujStracone(tempstring, posortowaneRoznice);
                //sortowanie po strzelonych
                //   posortowane.sort(new GraczPodsumowanieComparatorGoalScored());
                //   tempstring.addAll(posortowaneRoznice);
                //sortowanie posortowane  po straconych
            }
        }
    }

    protected void sortujStracone(List<GraczPodsumowanie> tempstring, List<GraczPodsumowanie> posortowane) {
        var listaIndeksowPowtorzonychStrzelonych = znajdzIndeksyPowtorzonychStrzelonych(posortowane);

        for (int j = 0; j < listaIndeksowPowtorzonychStrzelonych.size(); j++) {
            //sortowanie końcówki listy (ze względu na porównanie ostatniego indeksu z i+1)
            if (j == listaIndeksowPowtorzonychStrzelonych.size() - 1) {

                var posortowaneRoznice = posortowane.subList(listaIndeksowPowtorzonychStrzelonych.get(j), (posortowane.size()));
                // sortowanie po strzelonych
                posortowaneRoznice.sort(new GraczPodsumowanieComparatorGoalLost());
                tempstring.addAll(posortowaneRoznice);
            } else {
                var posortowaneRoznice = posortowane.subList(listaIndeksowPowtorzonychStrzelonych.get(j), listaIndeksowPowtorzonychStrzelonych.get(j + 1));
                posortowaneRoznice.sort(new GraczPodsumowanieComparatorGoalLost());
                //sortowanie po strzelonych
                //   posortowane.sort(new GraczPodsumowanieComparatorGoalScored());
                tempstring.addAll(posortowaneRoznice);
                //sortowanie posortowane  po straconych
            }
        }
    }


    protected List<Integer> znajdzIndeksyPowtorzonychRoznic(List<GraczPodsumowanie> gracze) {
        List<Integer> listaIndeksow1 = new ArrayList<>();
        listaIndeksow1.add(0);
        for (int ii = 0; ii < gracze.size() - 1; ii++) {
            if (!(gracze.get(ii + 1).getRoznica().equals(gracze.get(ii).getRoznica()))) {
                listaIndeksow1.add(ii + 1);
            }

        }
        return listaIndeksow1;
    }

    protected List<Integer> znajdzIndeksyPowtorzonychStrzelonych(List<GraczPodsumowanie> gracze) {
        List<Integer> listaIndeksow1 = new ArrayList<>();
        listaIndeksow1.add(0);
        for (int ii = 0; ii < gracze.size() - 1; ii++) {
            if (!(gracze.get(ii + 1).getGoleStrzelone().equals(gracze.get(ii).getGoleStrzelone()))) {
                listaIndeksow1.add(ii + 1);
            }

        }
        return listaIndeksow1;
    }

}
