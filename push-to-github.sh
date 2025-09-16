#!/bin/bash

# Script per push del progetto JReverse su GitHub
# Autore: JReverse Development Team
# Data: 13 Settembre 2025
# IMPORTANTE: Eseguire con: GITHUB_TOKEN=your_token ./push-to-github.sh

# Configurazione
GITHUB_USER="DenzoSOFTHub"
REPO_NAME="JReverse"

# Verifica che il token sia fornito come variabile d'ambiente
if [ -z "${GITHUB_TOKEN}" ]; then
    echo "❌ ERRORE: Token GitHub non fornito!"
    echo ""
    echo "Uso corretto:"
    echo "  export GITHUB_TOKEN=your_personal_access_token"
    echo "  ./push-to-github.sh"
    echo ""
    echo "Oppure:"
    echo "  GITHUB_TOKEN=your_personal_access_token ./push-to-github.sh"
    echo ""
    exit 1
fi

REPO_URL="https://${GITHUB_TOKEN}@github.com/${GITHUB_USER}/${REPO_NAME}.git"

# Estrai versione dal pom.xml principale
VERSION=$(grep -m1 '<version>' pom.xml | sed 's/.*<version>\(.*\)<\/version>.*/\1/')
BRANCH_NAME="v${VERSION}"

echo "========================================="
echo "   JReverse GitHub Push Script"
echo "========================================="
echo ""
echo "📦 Repository: ${GITHUB_USER}/${REPO_NAME}"
echo "📌 Version: ${VERSION}"
echo "🌿 Branch: ${BRANCH_NAME}"
echo ""

# Verifica se il repository esiste su GitHub
echo "🔍 Verifica esistenza repository su GitHub..."
HTTP_STATUS=$(curl -s -o /dev/null -w "%{http_code}" \
    -H "Authorization: token ${GITHUB_TOKEN}" \
    "https://api.github.com/repos/${GITHUB_USER}/${REPO_NAME}")

if [ "${HTTP_STATUS}" -eq "404" ]; then
    echo "📦 Repository non trovato. Creazione in corso..."
    
    # Crea il repository
    CREATE_RESPONSE=$(curl -s -X POST \
        -H "Authorization: token ${GITHUB_TOKEN}" \
        -H "Accept: application/vnd.github.v3+json" \
        "https://api.github.com/user/repos" \
        -d '{
            "name": "'${REPO_NAME}'",
            "description": "JReverse - Java Reverse Engineering Tool for JAR Analysis",
            "private": false,
            "has_issues": true,
            "has_projects": true,
            "has_wiki": true,
            "auto_init": false,
            "homepage": "https://denzosoft.com/jreverse"
        }')
    
    # Verifica se la creazione è andata a buon fine
    if echo "${CREATE_RESPONSE}" | grep -q "\"full_name\""; then
        echo "✅ Repository creato con successo!"
    else
        echo "❌ Errore nella creazione del repository:"
        echo "${CREATE_RESPONSE}"
        exit 1
    fi
else
    echo "✅ Repository esistente trovato"
fi

# Configura git se necessario
if [ ! -d ".git" ]; then
    echo "📝 Inizializzazione repository Git locale..."
    git init
    git config user.name "DenzoSOFT Development"
    git config user.email "development@denzosoft.com"
fi

# Rimuovi e ricrea remote
git remote remove origin 2>/dev/null || true
git remote add origin "${REPO_URL}"

# Aggiungi tutti i file (escluso questo script con il token)
echo "➕ Aggiunta file al repository..."
git add -A
git reset push-to-github.sh  # Escludi questo script dal commit

# Crea nuovo commit
COMMIT_MSG="Release ${VERSION} - Fase 2 Completata (100%)

Completamento Fase 2: Spring Boot Detection & Entrypoint Analysis
- 15/15 Analyzer implementati e testati
- 12/12 Report generators configurati
- 150+ unit test con >80% coverage
- ComponentScanAnalyzer implementato
- Factory registrations complete
- Documentazione allineata

Stato del progetto:
- Fase 1: ✅ Completata (Foundation & Core Infrastructure)
- Fase 2: ✅ Completata (Spring Boot Detection & Entrypoint Analysis)
- Pronto per Fase 3: Architecture & Dependency Analysis"

echo "💾 Creazione commit..."
git commit --amend -m "${COMMIT_MSG}" || git commit -m "${COMMIT_MSG}"

# Crea/switch al branch della versione
echo "🌿 Creazione/switch al branch ${BRANCH_NAME}..."
git checkout -b "${BRANCH_NAME}" 2>/dev/null || git checkout "${BRANCH_NAME}"

# Push del branch
echo ""
echo "🚀 Push del branch ${BRANCH_NAME} su GitHub..."
git push -u origin "${BRANCH_NAME}" --force

# Verifica se il push è andato a buon fine
if [ $? -eq 0 ]; then
    echo ""
    echo "========================================="
    echo "✅ PUSH COMPLETATO CON SUCCESSO!"
    echo "========================================="
    echo ""
    echo "📦 Repository: https://github.com/${GITHUB_USER}/${REPO_NAME}"
    echo "🌿 Branch: ${BRANCH_NAME}"
    echo "📌 Version: ${VERSION}"
    echo ""
    echo "🔗 Link diretti:"
    echo "   Repository: https://github.com/${GITHUB_USER}/${REPO_NAME}"
    echo "   Branch: https://github.com/${GITHUB_USER}/${REPO_NAME}/tree/${BRANCH_NAME}"
    echo ""
    
    # Crea anche tag per la versione
    echo "🏷️  Creazione tag ${VERSION}..."
    git tag -a "${VERSION}" -m "Release ${VERSION} - Fase 2 Completata" 2>/dev/null || true
    git push origin "${VERSION}" 2>/dev/null || true
    
else
    echo ""
    echo "❌ Errore durante il push. Verificare credenziali e connessione."
    exit 1
fi

# Push anche del branch main per riferimento
echo "🌿 Push anche del branch main..."
git checkout -b main 2>/dev/null || git checkout main
git merge "${BRANCH_NAME}" --no-edit
git push -u origin main --force

echo ""
echo "🎉 Operazione completata!"
echo "📊 Statistiche repository:"
git shortlog -sn --all | head -10
echo ""
echo "📈 Commit totali: $(git rev-list --all --count)"
echo "📁 File nel progetto: $(git ls-files | wc -l)"
echo ""