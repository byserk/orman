package org.orman.mapper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.orman.mapper.annotation.Id;
import org.orman.mapper.exception.DuplicateColumnNamesException;
import org.orman.mapper.exception.DuplicateTableNamesException;
import org.orman.mapper.exception.NotDeclaredIdException;
import org.orman.mapper.exception.TooManyIdException;
import org.orman.mapper.exception.UnmappedDataTypeException;
import org.orman.mapper.exception.UnmappedEntityException;
import org.orman.mapper.exception.UnmappedFieldException;

/**
 * Holds object mapping scheme and their physical table names.  
 * 
 * @author alp
 *
 */
public class PersistenceSchemeMapper {
	private Set<Entity> entities;
	private Map<String, Entity> tableNames; // no need for DoubleAssociativeMap

	/**
	 * Initializes an empty ORM scheme.
	 */
	public PersistenceSchemeMapper() {
		this.entities = new HashSet<Entity>();
		this.tableNames = new HashMap<String, Entity>();
	}

	/**
	 * Adds given entity to the mapping scheme.
	 * 
	 * Precondition: Physical table name should be binded.
	 * 
	 * @throws UnmappedEntityException if no physical name binding
	 * done before.
	 * @throws DuplicateTableNamesException if binded physical name already
	 * exists in the scheme.
	 */
	public void addEntity(Entity e) {
		if (e.getGeneratedName() == null || "".equals(e.getGeneratedName()))
			throw new UnmappedEntityException(e.getOriginalFullName());

		checkConflictingEntities(e); // exception stops the check.

		this.entities.add(e);
		this.tableNames.put(e.getGeneratedName(), e);
	}

	/**
	 * Returns entity with given physical table name.
	 * @param tblName case-sensitive physical table name.
	 * @return
	 */
	public Entity getEntityByTableName(String tblName) {
		return this.tableNames.get(tblName);
	}

	/**
	 * Returns {@link Entity} of given class type.
	 * @param entityClass
	 * @return null if not found.
	 */
	public Entity getBindedEntity(Class<?> entityClass) {
		for (Entity e : getEntities())
			if (e.getType().equals(entityClass))
				return e;
		return null;
	}

	/**
	 * Checks whether physical name of {@link Entity} <code>e</code> in usage.
	 * @return <code>true</code> if no conflict found, throws exception otherwise.
	 * @throws DuplicateTableNamesException if an entity with this physical name exists.
	 */
	private boolean checkConflictingEntities(Entity e) {
		for (Entity f : this.entities) {
			if (e != f && e.equals(f)) {
				throw new DuplicateTableNamesException(f.getOriginalName(), e
						.getOriginalName());
			}
		}
		return true;
	}

	/**
	 * @return all {@link Entity}s in scheme.
	 */
	public Set<Entity> getEntities() {
		return this.entities;
	}

	/**
	 * Precondition: Name and type should be binded.
	 * 
	 * @throws UnmappedFieldException
	 *             if physical column name is not binded.
	 * @throws UnmappedDataTypeException
	 *             if physical data type is not binded.
	 * @throws DuplicateColumnNamesException
	 *             if there exists more than one fields with same physical
	 *             column name
	 * 
	 * 
	 */
	public void checkConflictingFields(Entity e) {
		for (Field f : e.getFields()) {
			// unbinded column name
			if (f.getGeneratedName() == null || "".equals(f.getGeneratedName()))
				throw new UnmappedFieldException(f.getOriginalName());

			// unbinded column data type
			if (f.getType() == null) {
				throw new UnmappedDataTypeException(f.getOriginalName(), f
						.getClazz().getName());
			}

			// conflicting column name bindings
			for (Field g : e.getFields()) {
				if (f != g && f.equals(g)) {
					throw new DuplicateColumnNamesException(
							f.getOriginalName(), g.getOriginalName());
				}
			}
		}
	}

	/**
	 * Ensures that {@link Id} annotation exist exactly once in {@link Field}s
	 * of given {@link Entity}.
	 * 
	 * @throws TooManyIdException
	 *             if exists more than one.
	 * @throws NotDeclaredIdException
	 *             if does not exist any.
	 * @param e
	 */
	public void checkIdBinding(Entity e) {
		int idOccurrenceCount = 0;

		for (Field f : e.getFields()) {
			if (f.isId())
				idOccurrenceCount++;
		}

		if (idOccurrenceCount < 1)
			throw new NotDeclaredIdException(e.getOriginalFullName());
		else if (idOccurrenceCount > 1)
			throw new TooManyIdException(e.getOriginalFullName());
	}

	/**
	 * WARNING: Be cautious when two Entities have the same class name. e.g.
	 * com.app.model.User and com.app.model.administrative.User.
	 * 
	 * If more than occurrences found with same name, return value will be
	 * arbitrarily chosen.
	 * 
	 */
	public Entity getEntityByClassName(String className) {
		for(Entity e: getEntities()){
			if (e.getOriginalName().equals(className))
				return e;
		}
		return null;
	}

}
