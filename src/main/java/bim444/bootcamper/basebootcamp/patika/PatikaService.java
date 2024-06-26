package bim444.bootcamper.basebootcamp.patika;

import bim444.bootcamper.basebootcamp.BaseBootcamp;
import bim444.bootcamper.jsoup.PatikaInfoResponse;
import bim444.bootcamper.jsoup.PatikaScrapeData;
import bim444.bootcamper.language.LanguageService;
import bim444.bootcamper.mail.MailService;
import bim444.bootcamper.mail.SendMailRequest;
import bim444.bootcamper.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PatikaService {

    private final PatikaScrapeData patikaScrapeData;
    private final PatikaConverter patikaConverter;
    private final PatikaRepository patikaRepository;
    private final LanguageService languageService;
    private final MailService mailService;
    private final UserService userService;


    public List<PatikaResponse> getDatabaseBootcamp(){
        List<PatikaInfoResponse> patikaInfoResponseList = patikaScrapeData.scrapeLiveBootcamp();

        return getDatabasePatikaResponses(patikaInfoResponseList);
    }

    private List<PatikaResponse> getDatabasePatikaResponses(List<PatikaInfoResponse> patikaInfoResponseList) {
        //Patika tablosunu eski ve yeni patikalar olartak duzenle
        convertPatikaAndSave(patikaInfoResponseList,false);
        List<PatikaResponse> convert = patikaConverter.convert(patikaRepository.findAll());
        log.info("tum veriler getirildi ve PatikaResponse ye cevirildi");
        return convert;
    }

    public List<PatikaResponse> scrapeDeadBootcamp(){
        List<PatikaInfoResponse> patikaInfoResponseList = patikaScrapeData.scrapeDeadBootcamp();

        return getPatikaResponses(patikaInfoResponseList,true);
    }

    public List<PatikaResponse> scrapeLiveBootcamp(){
        List<PatikaInfoResponse> patikaInfoResponseList = patikaScrapeData.scrapeLiveBootcamp();

        return getPatikaResponses(patikaInfoResponseList,false);
    }

    private List<PatikaResponse> getPatikaResponses(List<PatikaInfoResponse> patikaInfoResponseList,Boolean isDead) {
        //Patika tablosunu eski ve yeni patikalar olartak duzenle
        List<Patika> patikaList = convertPatikaAndSave(patikaInfoResponseList,isDead);
        List<PatikaResponse> convert = patikaConverter.convert(patikaList);
        log.info("anlik ve PatikaResponse ye cevirildi");
        return convert;
    }


    private List<Patika> convertPatikaAndSave(List<PatikaInfoResponse> patikaInfoResponseList, Boolean isDead) {
        List<Patika> newList = new ArrayList<>();
        List<Patika> newBootcampList = new ArrayList<>();
        List<Patika> patikaList = patikaConverter.convertResponse(patikaInfoResponseList);
        log.info("patika objesine cevrildi");

        List<Patika> list = patikaList.stream().filter(patika -> findByName(patika.getName()) == null).toList();
        log.info("ayni veriler es gecildi");


        log.info("patika objeleri kaydedildi");

        List<Patika> list1 = patikaList.stream().filter(patika -> findByName(patika.getName()) != null).toList();
        List<Patika> getDatabaseConflictBootcamp = list1.stream().map(patika -> findByName(patika.getName())).toList();

        //eger bu methoda eski bootcampleri cagirirken girdiyse database dekilerle cakisan verileri al ve hepsinin isDeadini ture yap
        //bu sekilde eger cakisan verilerden birisi canli veriyse yani suanki bootcampin suresi bitmisse onu gecmis bootcam olarak isaretle
        if (isDead){
            getDatabaseConflictBootcamp.forEach(patika -> patika.setIsDead(true));
            patikaRepository.saveAll(getDatabaseConflictBootcamp);
        }
        log.info("ayni isme sahip olan veriler databaseden getirildi");

        if (!list.isEmpty() ){
             newBootcampList = patikaRepository.saveAll(list);
            List<Patika> finalNewBootcampList = newBootcampList;
            if ( !isDead){
                userService.findAll().forEach(user -> {
                    mailService.sendMail(new SendMailRequest(user.getEmail(),getPatikasUrl(finalNewBootcampList).toString(),"New Bootcamp"));
                });
            }

        }

        newList.addAll(newBootcampList);
        newList.addAll(getDatabaseConflictBootcamp);
        return newList;
    }

    private Patika findByName(String name){
        return patikaRepository.findByName(name).orElse(null);
    }

    private List<String> getPatikasUrl(List<Patika> patikaList){
       return patikaList.stream()
                .map(BaseBootcamp::getLink).toList();
    }
}
