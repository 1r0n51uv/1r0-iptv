package com.ir0.iptv.domain.dashboard

class MemoriaFocus {

    fun focusIniziale(
        righe: List<RigaDashboard>,
        ultimaChiave: String?,
        contenutoDiDefault: String?
    ): String? {
        val presenti = righe.flatMap { riga -> riga.contenuti.map { it.chiaveIdentita } }
        return listOfNotNull(ultimaChiave, contenutoDiDefault).firstOrNull { it in presenti }
            ?: presenti.firstOrNull()
    }
}
