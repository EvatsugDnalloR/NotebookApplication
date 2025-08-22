/**
 * The model module for the JavaFX NotebookApplication.
 *
 * <p>This module contains the core business logic and data structures for managing
 * notebooks, note groups, and individual note pages.
 * It implements the Observer pattern through property change support to enable UI synchronisation.</p>
 *
 * <h2>Key Components:</h2>
 * <ul>
 *   <li><b>NoteFacade</b> - Main facade class providing high-level operations for notebook management</li>
 *   <li><b>NoteGroup</b> - Represents a collection of note pages with group-level operations</li>
 *   <li><b>NotePage</b> - Represents an individual note page with content and styling</li>
 *   <li><b>TextSegment</b> - Immutable record for text with consistent styling</li>
 *   <li><b>NoteSubject</b> - Abstract base class providing property change support</li>
 *   <li><b>EventPropertyNameEnum</b> - Enumeration of property change event types</li>
 * </ul>
 *
 * <p>The module handles serialization/deserialization of notebook data and maintains
 * the complete state of the application's data model.</p>
 */
package notebookapplication.gui;