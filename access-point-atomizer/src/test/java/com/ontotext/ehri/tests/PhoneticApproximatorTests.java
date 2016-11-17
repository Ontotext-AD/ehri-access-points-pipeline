package com.ontotext.ehri.tests;

import com.ontotext.ehri.phapp.PhoneticApproximator;
import com.ontotext.ehri.phapp.PhoneticModel;
import org.apache.commons.codec.EncoderException;
import org.junit.Test;

import java.util.SortedMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class PhoneticApproximatorTests {

    @Test
    public void testApproximator() {
        PhoneticModel phomo = new PhoneticModel(4, 4, 0.5);
        String[] reykjavik = { "Горад Рэйкявік", "ରେକ୍ଜାଭିକ", "Ρευκιαβικ", "ქართული", "रेक्जाविक", "Reykyavik", "Reikiavike", "ਰੇਕਿਆਵਿਕ", "Rejkjaviko", "ریکجاوک", "ريكيافيك", "Reykjawik", "റെയിക്യാവിക്", "雷克雅維克", "Rejkjavik", "རེཀ་ཇ་བིཀ།", "Рейкьявик", "雷克亞維克", "রেইকিয়াভিক", "რეიკიავიკი", "رېيكياۋىك", "Reikjavik", "雷克雅未克", "Рейкиявик", "Reykiavica", "Рейкявік", "ڕێکیاڤیک", "Ρέικιαβικ", "Rēcwīc", "Рејкјавик", "Reiquiavique", "Reiquiavik", "Reikjavīka", "רעקיאוויק", "רייקיאוויק", "Rėikjavėks", "Reiciavicia", "Réicivíc", "ሬይኪያቪክ", "Reykjavik", "Reykjavík", "Reikiavik", "Рейкявик", "రేకవిక్", "レイキャヴィーク", "Reykjavíkur", "ریکیاوک", "Ռեյկյավիկ", "Rèkyavik", "Reikjavikas", "ரெய்க்யவிக்", "रेक्याविक", "เรคยาวิก", "Reykyabik", "레이캬비크", "ریکیاویک" };
        String[] zhitomir = { "Житомир", "ჟიტომირი", "Jytómyr", "Jytomyr", "ז'יטומיר", "Горад Жытомір", "זשיטאמיר", "Žitomir", "日托米尔", "Žõtomõr", "ژیتومیر", "جيتومير", "Zsitomir", "Schytomyr", "ジトームィル", "Жытомир", "Zhytomyr", "Ĵitomir", "Shitomir", "Jitomir", "Zjytomyr", "Żytomierz", "Zhitomir", "Jıtomır", "Ժիտոմիր", "Jîtomîr", "Žytomyr", "Житомиръ", "Zitomiria", "지토미르", "Žytomyras" };
        phomo.add(reykjavik);
        phomo.add(zhitomir);

        SortedMap<String, String> subs = phomo.substitutions(0.05, 0.2);
        PhoneticApproximator phapp = new PhoneticApproximator(subs);

        try {
            assertEquals("", phapp.encode("Рейкявик"));
            assertEquals("riikiavik", phapp.encode("Reiquiavique"));
            assertEquals("riikvik", phapp.encode("Reiciavicia"));
            assertEquals("riikiavik", phapp.encode("Reykjavík"));

            assertEquals("", phapp.encode("Житомир"));
            assertEquals("skhitimir", phapp.encode("Schytomyr"));
            assertEquals("iitimiri", phapp.encode("Żytomierz"));
            assertEquals("ihitimir", phapp.encode("Zhytomyr"));
        } catch (EncoderException e) {
            fail(e.getMessage());
        }
    }
}
