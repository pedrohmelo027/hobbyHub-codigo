const tagGeral = document.getElementById("tagGeralId");
const tagEspecifica = document.getElementById("tagEspecificaId");

function atualizarTagsEspecificas() {
    const idGeral = tagGeral.value;

    for (const opcao of tagEspecifica.options) {
        if (opcao.value === "") {
            continue;
        }

        const pertenceAGeral = opcao.dataset.tagGeralId === idGeral;
        opcao.hidden = !pertenceAGeral;
        opcao.disabled = !pertenceAGeral;

        if (!pertenceAGeral && opcao.selected) {
            tagEspecifica.value = "";
        }
    }

    tagEspecifica.disabled = idGeral === "";
}

if (tagGeral && tagEspecifica) {
    tagGeral.addEventListener("change", atualizarTagsEspecificas);
    atualizarTagsEspecificas();
}
