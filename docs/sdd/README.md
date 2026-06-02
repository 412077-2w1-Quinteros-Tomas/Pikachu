# Software Design Document - Pokémon TCG Digital

## Índice

| # | Documento | Descripción |
|---|---|---|
| 00 | [Requisitos](00-requisitos.md) | RF-01 a RF-07 + RNF-01 a RNF-06 detallados (referencia principal) |
| 01 | [Overview](01-overview.md) | Visión del proyecto, stack tecnológico, objetivos, puntuación |
| 02 | [Arquitectura](02-arquitectura.md) | Arquitectura general, patrones de diseño, diagramas |
| 03 | [Modelo de Dominio](03-modelo-dominio.md) | Entidades, modelos del engine, enums, relaciones |
| 04 | [API Design](04-api-design.md) | Endpoints REST + protocolo WebSocket |
| 05 | [Game Engine](05-game-engine.md) | Máquina de estados, pipeline de combate, efectos de estado, reglas |
| 06 | [Frontend](06-frontend.md) | Features, routing, signals, componentes |
| 07 | [Base de Datos](07-base-datos.md) | Schema SQL, configuración H2, perfil PostgreSQL |
| 08 | [Plan de Implementación](08-plan-implementacion.md) | Cronograma 6 semanas, tareas por semana, entregables |
| 09 | [Plan de Ramas Git](09-plan-ramas-git.md) | Estrategia de branching, asignación por persona, PRs |
| 10 | [Testing](10-testing.md) | Estrategia de testing, cobertura, componentes críticos |
| 11 | [Seguridad](11-seguridad.md) | Validaciones, ocultamiento de info sensible |
| 12 | [Riesgos](12-riesgos.md) | Riesgos identificados y mitigaciones |
| 13 | [Checklist de Entrega](13-checklist-entrega.md) | Checklist de puntuación + requisitos obligatorios |
| 14 | [Post-Proyecto](14-post-proyecto.md) | Funcionalidades bonus (descartadas del plan inicial) |

---

## Estado del Documento

| Campo | Valor |
|---|---|
| **Versión** | 1.0 |
| **Fecha** | Mayo 2026 |
| **Proyecto** | Pokémon Trading Card Game Digital |
| **Materia** | Programación III - UTN FRC - TUP |
| **Equipo** | 6 integrantes |
| **Cronograma** | 6 semanas |

---

## Notas de Uso

Este SDD es un **documento vivo**. Se espera que evolucione durante el desarrollo. Las secciones marcadas como "sujetas a cambio" se revisarán semanalmente al momento de hacer merge a `develop`.

### Secciones con mayor probabilidad de cambio

1. **Game Engine** (`05-game-engine.md`) - Modelos en memoria y pipeline de combate
2. **API Design** (`04-api-design.md`) - Protocolo WebSocket y DTOs
3. **Plan de Implementación** (`08-plan-implementacion.md`) - Cronograma semanal

### Log de Cambios

| Versión | Fecha | Descripción |
|---|---|---|
| 1.0 | Mayo 2026 | Documento inicial |
