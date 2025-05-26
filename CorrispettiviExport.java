package it.siae.corrispettivi.integration.model.scaricocorrispettivi;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CorrispettiviExport {
    private String tipoRecord;
    private String seprag;
    private Integer dataContabile;
    private String progressivoOrdine;
    private String sistema = "SUN";
    private String flagProiezioneNonCinema;
    private String flagProiezione3D;
    private String stato;
    private String codiceGenereEvento;
    private String descrizioneGenereEvento;
    private String denominazioneOrganizzatore;
    private String pivaCfOrganizzatore;
    private String descrizioneProvinciaOrganizzatore;
    private String indirizzoOrganizzatore;
    private String capOrganizzatore;
    private Date dataInizio;
    private Date dataFine;
    private String descrizioneComuneOrganizzatore;
    private String domicilioFiscale;
    private String domicilioFiscale398;
    private String indicatore398;
    private Double totaliCorrispettivi;
    private Double totaliCorrispettivi398;
    private Integer titoliEsitati;
    private Double sumIncassoLordoBiglietti;
    private Double corrispettivoAbbonamentiVenduti;
    private Integer numeroAbbonamentiVenduti;
    private Integer numeroRateoAbbonamenti;
    private String tipoProventoLordo;
    private Double impostaIntrattBigliettiOmaggio;
    private Double ivaBigliettiOmaggio;
    private Double corrispettivoOmaggioImpostaIntratt;
    private Double corrispettivoOmaggioIva;
    private Integer numeroEventi;
    private String codiceComuneSiae;
    private String codiceSpeiSpazio;
    private String denominazioneLocaleSpazio;
    private String indirizzoLocale;
    private String capLocale;
    private String dataRettifica;
    private String squadraAvversaria;
    private String flagCampionato;
    private String flagDistintaDichiarazione;
    private String flagDichiarazioneDUfficio;
    private Integer numeroDistintaDichiarazione;
    private Integer numeroReversale;
    private String dataReversale;
    private String numeroRiferimentoDocumentoRettifica;
    private String codiceBASpazio;
    private String codiceSpeiCcw;
}
