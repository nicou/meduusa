package fi.softala.meduusatunnit.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import fi.softala.meduusatunnit.bean.Kayttaja;
import fi.softala.meduusatunnit.bean.KayttajaImpl;
import fi.softala.meduusatunnit.bean.Merkinta;
import fi.softala.meduusatunnit.bean.MerkintaImpl;
import fi.softala.meduusatunnit.dao.MerkintaDAO;
import fi.softala.meduusatunnit.utility.MerkintaJarjestaja;
import fi.softala.meduusatunnit.utility.Slack;

@Controller
public class TuntiKontrolleri {

	@Inject
	private MerkintaDAO dao;

	public MerkintaDAO getDao() {
		return dao;
	}

	public void setDao(MerkintaDAO dao) {
		this.dao = dao;
	}

	final static Logger logger = LoggerFactory
			.getLogger(TuntiKontrolleri.class);

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String naytaEtusivu(Model model, String id) {

		// Näytetään etusivu

		// Kutsutaan DAO-luokkaa ja haetaan merkinnät
		List<Merkinta> merkinnat = dao.haeKaikkiMerkinnat();

		String naytettavat = "kaikki";

		// Kikkaillaan totalit omaksi listaksi, joku muu saa miettiä tähän
		// fiksumpaa tapaa
		List<Merkinta> tiimintunnit = new ArrayList<>();

		for (int i = 0; i < merkinnat.size(); i++) {

			if (tiimintunnit.size() == 0) {
				Merkinta merkinta = new MerkintaImpl();
				merkinta.setKayttaja(merkinnat.get(i).getKayttaja());
				merkinta.setTunnit(merkinnat.get(i).getTunnit());
				tiimintunnit.add(merkinta);
			} else {
				boolean loytyi = false;
				for (int j = 0; j < tiimintunnit.size(); j++) {
					if (merkinnat.get(i).getKayttaja().getId() == tiimintunnit
							.get(j).getKayttaja().getId()) {
						tiimintunnit.get(j).setTunnit(
								tiimintunnit.get(j).getTunnit()
										+ merkinnat.get(i).getTunnit());
						j = tiimintunnit.size();
						loytyi = true;
					}
				}
				if (!loytyi) {
					Merkinta merkinta = new MerkintaImpl();
					merkinta.setKayttaja(merkinnat.get(i).getKayttaja());
					merkinta.setTunnit(merkinnat.get(i).getTunnit());
					tiimintunnit.add(merkinta);
				}
			}
		}
		
		// Jos on saatu doGet-metodilta id-parametri, katsotaan löytyykö vastaavaa
		// id:tä käyttäjälistalta
		if (id != null) {
			List<Merkinta> kayttajanMerkinnat = new ArrayList<Merkinta>();
			
			for (int i = 0; i < merkinnat.size(); i++) {
				if (String.valueOf(merkinnat.get(i).getKayttaja().getId()).equals(id)) {
					kayttajanMerkinnat.add(merkinnat.get(i));
				}
			}
			
			if (kayttajanMerkinnat.size() > 0) {
				merkinnat = kayttajanMerkinnat;
				naytettavat = "kayttaja";
			}
		}

		// Järjestetään tiimin tunnit suurimmasta määrästä pienimpään
		Collections.sort(tiimintunnit, new MerkintaJarjestaja());
		model.addAttribute("merkinnat", merkinnat);
		model.addAttribute("tiimintunnit", tiimintunnit);
		model.addAttribute("naytettavat", naytettavat);

		return "sivu";
	}

	@RequestMapping(value = "/", method = RequestMethod.POST)
	public String lisaaTunteja(Model model,
			@RequestParam(value = "nimi", required = true) String nimi,
			@RequestParam(value = "tunnit", required = true) String tunnit,
			@RequestParam(value = "minuutit", required = true) String minuutit,
			@RequestParam(value = "kuvaus", required = false) String kuvaus,
			@RequestParam(value = "slack", required = false) String slack[]) {

		String viesti = null;

		try {
			// Parsitaan integereiksi, voisi vetästä myös suoraan doubleksi
			int tunnitInt = Integer.parseInt(tunnit);
			int minuutitInt = Integer.parseInt(minuutit);

			// Tehdään pienimuotoinen validointi
			if ((tunnitInt >= 0 && tunnitInt < 13 && minuutitInt >= 0 && minuutitInt < 60)
					&& tunnitInt + minuutitInt > 0) {
				// Lasketaan tuntimäärä tunneista ja minuuteista doubleksi
				double tunnitYht = Double.valueOf(tunnitInt)
						+ (Double.valueOf(minuutitInt) / 60);

				// Muodostetaan olio
				Merkinta merkinta = new MerkintaImpl();
				Kayttaja kayttaja = new KayttajaImpl();
				kayttaja.setSahkoposti(nimi);
				merkinta.setKayttaja(kayttaja);
				merkinta.setTunnit(tunnitYht);
				merkinta.setKuvaus(kuvaus);

				// Lähetetään pyyntö DAO-luokkaan
				int rivit = dao.tallennaMerkinta(merkinta);

				// Katsotaan vastaus, näytetään sen mukaan sivulla viesti
				if (rivit == 1) {
					logger.info("Lisättiin " + tunnitYht + "h henkilölle "
							+ nimi);

					// Jos Slack-checkbox valittu niin viesti Slackiin
					if (slack != null && slack[0] != null
							&& slack[0].equals("yes")) {
						Slack slackbot = new Slack();
						String slackviesti = nimi + " kirjasti " + tunnit + "h";
						if (minuutitInt > 0) {
							slackviesti += " " + minuutit + "min";
						}
						slackviesti += " työtä (" + kuvaus + ")";
						slackbot.lahetaViesti(slackviesti);
					}

					viesti = "Merkintä tallennettu onnistuneesti!";

				} else if (rivit == 0) {
					logger.debug("Ei onnistunut - rivit = " + rivit);
					viesti = "Merkintää tallentaessa tapahtui virhe!";
				} else {
					logger.debug("Merkintää tallentaessa tapahtui ODOTTAMATON virhe!");
					viesti = "Merkintää tallentaessa tapahtui ODOTTAMATON virhe!";
				}
			} else {
				viesti = "Virhe! Tuntien määrä oltava 1-12h";
			}

		} catch (Exception ex) {
			logger.debug("Errori: " + ex);
		}
			
		if (viesti != null) {
			model.addAttribute("viesti", viesti);
		}	
		return naytaEtusivu(model, null);
	}

	@RequestMapping(value = "/henkilo/{id}", method = RequestMethod.GET)
	public String naytaKayttaja(@PathVariable Integer id, Model model) {
		
		return naytaEtusivu(model, String.valueOf(id));
	}
	
	@RequestMapping(value = "/poista/{id}", method = RequestMethod.GET)
	public String poistaMerkinta(@PathVariable Integer id, Model model) {
	
		int rivit = dao.poistaMerkinta(id);
		
		String viesti = null;
		
		if (rivit == 1) {
			viesti = "Merkintä poistettu onnistuneesti!";
		} else if (rivit == 0) {
			viesti = "Merkintää poistaessa tapahtui virhe!";
		}
		else {
			viesti = "Merkintää poistaessa tapahtui jotain odottamatonta!";
		}
		
		if (viesti != null) {
			model.addAttribute("viesti", viesti);
		}

		return naytaEtusivu(model, null);
	}
}