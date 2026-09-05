package com.ir0.iptv.domain.dashboard

class MemoriaFocus {

    /** Il focus all'avvio va sempre sul contenuto da riprendere (riga Continua a guardare),
     * mai su un punto qualunque dove si era rimasti: e' la stessa priorita' della banda in
     * evidenza in Dashboard, cosi' il D-pad parte sempre su cio' che il video mostra per primo. */
    fun focusIniziale(
        righe: List<RigaDashboard>,
        contenutoDiDefault: String?
    ): String? {
        val presenti = righe.flatMap { riga -> riga.contenuti.map { it.chiaveIdentita } }
        val continua = righe.firstOrNull { it.tipo == TipoRiga.CONTINUA }?.contenuti?.firstOrNull()?.chiaveIdentita
        return listOfNotNull(continua, contenutoDiDefault).firstOrNull { it in presenti }
            ?: presenti.firstOrNull()
    }
}
