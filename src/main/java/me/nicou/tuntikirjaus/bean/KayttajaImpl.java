package me.nicou.tuntikirjaus.bean;

public class KayttajaImpl implements Kayttaja {

	int id;
	String sahkoposti;
	String etunimi;
	String sukunimi;
	String salasana;

	public KayttajaImpl() {
		super();
	}

	public KayttajaImpl(int id, String sahkoposti, String etunimi, String sukunimi, String salasana) {
		super();
		this.id = id;
		this.sahkoposti = sahkoposti;
		this.etunimi = etunimi;
		this.sukunimi = sukunimi;
		this.salasana = salasana;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getSahkoposti() {
		return sahkoposti;
	}

	public void setSahkoposti(String sahkoposti) {
		this.sahkoposti = sahkoposti;
	}

	public String getEtunimi() {
		return etunimi;
	}

	public void setEtunimi(String etunimi) {
		this.etunimi = etunimi;
	}

	public String getSukunimi() {
		return sukunimi;
	}

	public void setSukunimi(String sukunimi) {
		this.sukunimi = sukunimi;
	}

	public String getSalasana() {
		return salasana;
	}

	public void setSalasana(String salasana) {
		this.salasana = salasana;
	}

	@Override
	public String toString() {
		return "KayttajaImpl [id=" + id + ", sahkoposti=" + sahkoposti + ", etunimi=" + etunimi + ", sukunimi="
				+ sukunimi + "]";
	}

}
