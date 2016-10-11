package me.nicou.tuntikirjaus.dao;

import java.util.List;

import me.nicou.tuntikirjaus.bean.Merkinta;
import me.nicou.tuntikirjaus.bean.Projekti;

public interface ProjektiDao {
	
	public List<Projekti> haeKayttajanProjektit(String sahkoposti);
	
	public Projekti haeProjektinTiedot(int projektiId, String sahkoposti);
	
	public Projekti haeProjektinTiedotKayttajalta(int projektiId, String sahkoposti, int kayttajaId);
	
	public int tallennaMerkinta(int projektiId, Merkinta merkinta);
	
	public int poistaKayttajanMerkinta(int merkintaId, String sahkoposti);
	
	public List<Merkinta> haeProjektinYhteistunnit(int projektiId);

}
