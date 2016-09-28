package com.ontotext.ehri.tests;

import org.apache.commons.codec.EncoderException;
import org.junit.Test;

import static com.ontotext.ehri.AccessPointAtomizerPR.encodePhonetics;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class PhoneticEncodingTests {

    @Test
    public void testSlavic() {
        phoneticallyIdentical("Zhytomyr", "Zhitomir");
        phoneticallyIdentical("Zhytomyr", "Shitomir");
        phoneticallyIdentical("Zhytomyr", "Schytomyr");
        phoneticallyIdentical("Zhytomyr", "Žitomir");
        phoneticallyIdentical("Zhytomyr", "Житомир");
        phoneticallyIdentical("Zhytomyr", "Житомиръ");
        phoneticallyIdentical("Zhytomyr", "Жытомир");

        phoneticallyIdentical("Vinnytsya", "Vinnitsa");
        phoneticallyIdentical("Vinnytsya", "Vinnytsya");
        phoneticallyIdentical("Vinnytsya", "Winniza");
        phoneticallyIdentical("Vinnytsya", "Винница");
        phoneticallyIdentical("Vinnytsya", "Вінниця");

        phoneticallyIdentical("Dniprodzerzhyns'k", "Dneprodsershinsk");
        phoneticallyIdentical("Dniprodzerzhyns'k", "Dneprodzerzhinsk");
        phoneticallyIdentical("Dniprodzerzhyns'k", "Dneprodzherzhinsk");
        phoneticallyIdentical("Dniprodzerzhyns'k", "Dnieprodzerzhynsk");
        phoneticallyIdentical("Dniprodzerzhyns'k", "Dniprodzerzhynsk");
        phoneticallyIdentical("Dniprodzerzhyns'k", "Dniprodzerzhyns’k");
        phoneticallyIdentical("Dniprodzerzhyns'k", "Днепродзержинск");

        phoneticallyIdentical("Dobrich", "Dobrič");
        phoneticallyIdentical("Dobrich", "Dobritch");
        phoneticallyIdentical("Dobrich", "Dobritsch");
        phoneticallyIdentical("Dobrich", "Dobricz");
        phoneticallyIdentical("Dobrich", "Добрич");

        phoneticallyIdentical("Zrenjanin", "Зренянин");
        phoneticallyIdentical("Zrenjanin", "Зрењанин");

        phoneticallyIdentical("Trešnjevac", "Трешњевац");

        phoneticallyIdentical("Koprivnica", "Koprivnicza");
        phoneticallyIdentical("Koprivnica", "Kopriwnitza");
        phoneticallyIdentical("Koprivnica", "Копривница");
        phoneticallyIdentical("Koprivnica", "Копривниця");
        phoneticallyIdentical("Koprivnica", "Копрівніця");

        phoneticallyIdentical("Nizhny Novgorod", "Nischni Nowgorod");
        phoneticallyIdentical("Nizhny Novgorod", "Nishni-Nowgorod");
        phoneticallyIdentical("Nizhny Novgorod", "Nizhnii Novgorod");
        phoneticallyIdentical("Nizhny Novgorod", "Nizhni Novgorod");
        phoneticallyIdentical("Nizhny Novgorod", "Nizhniy Novgorod");
        phoneticallyIdentical("Nizhny Novgorod", "Nižnij Nowgorod");
        phoneticallyIdentical("Nizhny Novgorod", "Нижний Новгород");
    }

    @Test
    public void testHebrew() {
        phoneticallyIdentical("Qiryat Yam", "Kiryat Yam");
        phoneticallyIdentical("Qiryat Yam", "Kirjat Jam");
        phoneticallyIdentical("Qiryat Yam", "Кирьят-Ям");
        phoneticallyIdentical("Qiryat Yam", "קריית ים");
        phoneticallyIdentical("Qiryat Yam", "קרית ימ");

        phoneticallyIdentical("Bet She'an", "Beisān");
        phoneticallyIdentical("Bet She'an", "Beit Shean");
        phoneticallyIdentical("Bet She'an", "Beit She'an");
        phoneticallyIdentical("Bet She'an", "Beït Shéan");
        phoneticallyIdentical("Bet She'an", "Bet Sche’an");
        phoneticallyIdentical("Bet She'an", "بيسان");
        phoneticallyIdentical("Bet She'an", "בית שאן");
    }

    private static void phoneticallyIdentical(String a, String b) {

        try {
            assertEquals("\"" + a + "\" should be phonetically identical to \"" + b + "\"", encodePhonetics(a), encodePhonetics(b));
        } catch (EncoderException e) {
            fail(e.getMessage());
        }
    }
}
