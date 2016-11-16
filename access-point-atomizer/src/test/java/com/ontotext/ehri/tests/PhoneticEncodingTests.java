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
        phoneticallyIdentical("Vinnytsya", "Винница");
        phoneticallyIdentical("Vinnytsya", "Вінниця");

        phoneticallyIdentical("Dneprodzerzhinsk", "Dneprodsershinsk");
        phoneticallyIdentical("Dneprodzerzhinsk", "Dneprodzherzhinsk");
        phoneticallyIdentical("Dneprodzerzhinsk", "Dnieprodzerzhynsk");
        phoneticallyIdentical("Dneprodzerzhinsk", "Днепродзержинск");

        phoneticallyIdentical("Zrenjanin", "Зренянин");
        phoneticallyIdentical("Zrenjanin", "Зрењанин");

        phoneticallyIdentical("Koprivnitsa", "Kopriwnitza");
        phoneticallyIdentical("Koprivnitsa", "Копривница");
        phoneticallyIdentical("Koprivnitsa", "Копривниця");
        phoneticallyIdentical("Koprivnitsa", "Копрівніця");

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

        phoneticallyIdentical("Bet She'an", "Beit Shean");
        phoneticallyIdentical("Bet She'an", "Beit She'an");
        phoneticallyIdentical("Bet She'an", "Beït Shéan");
        phoneticallyIdentical("Bet She'an", "Bet Sche’an");
    }

    private static void phoneticallyIdentical(String a, String b) {

        try {
            assertEquals("\"" + a + "\" should be phonetically identical to \"" + b + "\"", encodePhonetics(a), encodePhonetics(b));
        } catch (EncoderException e) {
            fail(e.getMessage());
        }
    }
}
