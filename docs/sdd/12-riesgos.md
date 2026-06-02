# 12 - Riesgos

## Riesgos Identificados

### Alta Probabilidad

| Riesgo | Impacto | Probabilidad | Mitigación |
|---|---|---|---|
| **Modelos del engine requieren cambios** | Alto | Alta | Los modelos en memoria (`engine/models/`) se ajustarán según edge cases. Se revisan semanalmente. |
| **Protocolo WebSocket cambia durante integración** | Alto | Alta | Se define un protocolo mínimo inicial y se extiende según necesidad. Se documentan los cambios. |
| **Cronograma se ajusta por complejidad del engine** | Medio | Alta | Buffer en semana 6. Priorizar reglas obligatorias sobre detalles. |
| **Bugs en reglas complejas del juego** | Alto | Alta | Tests unitarios extensivos del engine. Enfoque TDD para el pipeline de combate. |

### Media Probabilidad

| Riesgo | Impacto | Probabilidad | Mitigación |
|---|---|---|---|
| **API endpoints requieren ajustes** | Medio | Media | Los endpoints REST están bien definidos. Cambios menores esperados en DTOs. |
| **Schema de game_states (JSONB) cambia** | Medio | Media | La estructura del JSON se define cuando el engine está funcional. Flexible por naturaleza de JSON. |
| **Componentes FE necesitan reestructuración** | Bajo | Media | Algunos componentes pueden dividirse en sub-componentes. Bajo impacto. |
| **Conflictos de integración entre ramas** | Medio | Media | Rebase semanal antes de PR. CI automático detecta problemas. |

### Baja Probabilidad

| Riesgo | Impacto | Probabilidad | Mitigación |
|---|---|---|---|
| **Rate limits de pokemontcg.io API** | Alto | Baja | Caché local de todas las cartas. Sync una sola vez al inicio. |
| **Caída de conexión WebSocket** | Alto | Baja | Lógica de reconexión en `GameWebsocketService`. Resync de estado al reconectar. |
| **Entidades JPA requieren cambios mayores** | Medio | Baja | Bien definidas por el modelo relacional. Cambios menores esperados (índices, constraints). |
| **Patrones de diseño no son adecuados** | Alto | Baja | Los patrones elegidos (State, Strategy, CoR) son estándar para este tipo de problema. |

## Plan de Mitigación General

### Revisión Semanal de Riesgos

Cada viernes, al momento de hacer PR a `develop`:

1. ¿Se identificaron nuevos riesgos?
2. ¿Los riesgos existentes cambiaron de probabilidad o impacto?
3. ¿Las mitigaciones funcionaron?
4. ¿Se necesitan ajustes al plan?

### Estrategia de Contingencia

Si el cronograma se aprieta significativamente:

| Prioridad | Acción |
|---|---|
| 1 | Completar Game Engine con reglas básicas (sin edge cases) |
| 2 | Completar Game Board UI funcional (sin polish visual) |
| 3 | Completar Deck Builder con validación básica |
| 4 | Completar Lobby + WebSocket básico |
| 5 | Testing mínimo (JaCoCo 80%) |
| 6 | Documentation mínima |

Si el tiempo es extremadamente limitado, se pueden simplificar:

- Condiciones especiales (implementar solo 2-3 en lugar de 5)
- Tipos de Trainer (implementar Item y Supporter primero)
- Mega Evolution (es opcional según las reglas)
