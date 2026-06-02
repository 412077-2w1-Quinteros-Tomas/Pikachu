# 01 - Overview

## Visión del Proyecto

Desarrollo de una versión digital del Pokémon Trading Card Game (TCG) para 2 jugadores en tiempo real, siguiendo las reglas oficiales del set XY1. El proyecto implementa tanto el front-end como el back-end de la aplicación, integrando lógica de negocio con una interfaz clara y funcional.

## Stack Tecnológico

| Capa | Tecnología | Versión |
|---|---|---|
| **Backend** | Java + Spring Boot | Java 21, Spring Boot 4.0 |
| **Frontend** | Angular + TypeScript | Angular 20, TS 5.9 |
| **Base de Datos (dev)** | H2 Database | In-memory |
| **Base de Datos (prod)** | PostgreSQL | Última estable |
| **Comunicación Real-time** | WebSocket | STOMP |
| **API Externa** | pokemontcg.io | v2 |
| **Build** | Maven + npm | - |
| **Testing** | JUnit 5, Mockito, JaCoCo, Jasmine | - |
| **Control de Versiones** | Git + GitHub | GitFlow |

## Objetivos

### Obligatorio (100 puntos)

| Categoría | Puntos | Descripción |
|---|---|---|
| Reglas del Juego (RF-01 a RF-07) | 40 | Implementación completa de las reglas del Pokémon TCG |
| Arquitectura & Código | 25 | SOLID, patrones de diseño, código limpio |
| Base de Datos | 10 | Diseño relacional, migraciones versionadas |
| Frontend | 15 | UI funcional, interactiva, responsive |
| Testing | 10 | JaCoCo ≥ 80% global, ≥ 90% crítico, 1+ E2E |
| **Total** | **100** | Mínimo 60 para aprobar |

### Mínimo para Aprobar

- **60%** de los puntos obligatorios
- Los puntos opcionales solo cuentan si se cumplen todos los obligatorios

## Alcance

### Incluye

- Sistema de cartas con datos reales (pokemontcg.io API)
- Deck Builder con validación de reglas
- Lobby para crear y unirse a partidas
- Juego en tiempo real con WebSocket
- Motor de juego con todas las reglas oficiales
- Persistencia del estado de partida
- Pokedex para consultar cartas

### No Incluye (Plan Inicial)

- Animaciones avanzadas
- Sistema de ranking
- Chat en partida
- Mazos temáticos precargados

Estas funcionalidades se documentan en [14-post-proyecto.md](14-post-proyecto.md).

## Estado Actual del Proyecto

### Backend

- **88 archivos Java** existentes (todos stubs vacíos de 3 líneas)
- **4 archivos implementados**: Application.java, PingController.java, SpringDocConfig.java, MappersConfig.java, ErrorApi.java
- Estructura de paquetes completamente planificada
- H2 configurado en `application.properties`
- Dependencias de Maven listas (JPA, H2, Lombok, ModelMapper, SpringDoc, Validation, Actuator)

### Frontend

- **~35+ archivos TypeScript** existentes (casi todos stubs)
- Solo `app.routes.ts` y `app.config.ts` funcionales (mínimos)
- Solo `PokedexPage` tiene contenido placeholder
- Arquitectura feature-driven con 4 features planificadas
- Convención de nombres: archivos cortos (`.ts`) en lugar de `.component.ts`

## Referencias

- [Reglas oficiales XY1](../../materiales/xy1-rulebook-es.pdf)
- [Especificación del proyecto](../../materiales/TUP_3C_PIII_TPI_POKEMON_TCG.pdf)
- [API pokemontcg.io](https://pokemontcg.io/)
- [Portal de desarrolladores pokemontcg.io](https://dev.pokemontcg.io/)
