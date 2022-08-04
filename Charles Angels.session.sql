TRUNCATE houses CASCADE;
TRUNCATE children CASCADE;
DELETE FROM personal_information WHERE id_children IS NOT NULL;
TRUNCATE children_houses CASCADE;

SELECT * FROM children INNER JOIN personal_information pi ON pi.id_children = id;

SELECT h.id, COUNT(c.*) FROM houses h
LEFT JOIN children_houses ch ON ch.house_id = h.id
LEFT JOIN children c ON ch.child_id = c.id
GROUP BY h.id

SELECT COUNT(c.*) FROM children c
INNER JOIN children_houses ch ON ch.child_id = c.id

SELECT c.*, pi.birthdate, h.maximum_age
FROM children c
INNER JOIN children_houses ch ON ch.child_id = c.id
INNER JOIN houses h ON ch.house_id = h.id
INNER JOIN personal_information pi ON c.id = pi.id_children
WHERE ((pi.birthdate + interval ''1 year'' * (1 + h.maximum_age)) - now()) <= interval ''6 month''

/* Children with Family */

SELECT c.id, CONCAT(pi.name, ' ', pi.lastname), ARRAY(
    SELECT ARRAY[CONCAT(fpi.name, ' ', fpi.lastname), f.id::text]
    FROM children f
    INNER JOIN children_houses fh ON fh.child_id = f.id
    INNER JOIN personal_information fpi ON f.id = fpi.id_children
    WHERE EXISTS (
        SELECT 1
        FROM related_beneficiaries 
        WHERE (child_id = c.id AND related_id = f.id) 
              OR (child_id = f.id AND related_id = c.id)
    ) AND fh.house_id = ch.house_id
) as family, h.id, h.name
FROM children c
INNER JOIN children_houses ch ON ch.child_id = c.id
INNER JOIN personal_information pi ON c.id = pi.id_children
INNER JOIN houses h ON ch.house_id = h.id
WHERE EXISTS (SELECT 1
        FROM related_beneficiaries 
        WHERE child_id = c.id OR related_id = c.id)

SELECT * FROM related_beneficiaries

/* Clothing information */

SELECT 
       short_or_trousers_info[1], short_or_trousers_info[2],
       tshirt_or_shirt_info[1], tshirt_or_shirt_info[2],
       sweater_info[1], sweater_info[2],
       dress_info[1], dress_info[2],
       footwear_info[1], footwear_info[2]
FROM crosstab('
    SELECT raw_clothing.ind, raw_clothing.dress_type, ARRAY[raw_clothing.size, raw_clothing.amount] FROM (
        SELECT ROW_NUMBER() OVER (ORDER BY 1) as ind, ''short_or_trousers'' as dress_type, a.short_or_trousers_size as size, COUNT(*) as amount
        FROM children c
        INNER JOIN attires a ON a.id_child = c.id
        GROUP BY a.short_or_trousers_size

        UNION

        SELECT ROW_NUMBER() OVER (ORDER BY 1) as ind, ''tshirt_or_shirt'' as dress_type, a.tshirt_or_shirt_size as size, COUNT(*) as amount
        FROM children c
        INNER JOIN attires a ON a.id_child = c.id
        GROUP BY a.tshirt_or_shirt_size

        UNION

        SELECT ROW_NUMBER() OVER (ORDER BY 1) as ind, ''sweater'' as dress_type, a.sweater_size as size, COUNT(*) as amount
        FROM children c
        INNER JOIN attires a ON a.id_child = c.id
        WHERE a.sweater_size IS NOT NULL
        GROUP BY a.sweater_size

        UNION

        SELECT ROW_NUMBER() OVER (ORDER BY 1) as ind, ''dress'' as dress_type, a.dress_size as size, COUNT(*) as amount
        FROM children c
        INNER JOIN attires a ON a.id_child = c.id
        WHERE a.dress_size IS NOT NULL
        GROUP BY a.dress_size

        UNION

        SELECT ROW_NUMBER() OVER (ORDER BY 1) as ind, ''footwear'' as dress_type, a.footwear_size as size, COUNT(*) as amount
        FROM children c
        INNER JOIN attires a ON a.id_child = c.id
        GROUP BY a.footwear_size

    ) raw_clothing
    ORDER BY raw_clothing.ind
', '
    SELECT *
    FROM (VALUES
        (''short_or_trousers''),
        (''tshirt_or_shirt''),
        (''sweater''),
        (''dress''),
        (''footwear'')
    ) t(dress_type)
') AS (
    index INT, 
    short_or_trousers_info bigint[],
    tshirt_or_shirt_info bigint[],
    sweater_info bigint[],
    dress_info bigint[],
    footwear_info bigint[]
) ORDER BY (CASE COALESCE(short_or_trousers_info[1], 0)
                WHEN 0 THEN 0
                ELSE 1
            END) + 
            (CASE COALESCE(tshirt_or_shirt_info[1], 0)
                WHEN 0 THEN 0
                ELSE 1
            END) + 
            (CASE COALESCE(sweater_info[1], 0)
                WHEN 0 THEN 0
                ELSE 1
            END) + 
            (CASE COALESCE(dress_info[1], 0)
                WHEN 0 THEN 0
                ELSE 1
            END) + 
            (CASE COALESCE(footwear_info[1], 0)
                WHEN 0 THEN 0
                ELSE 1
            END) DESC


SELECT 
       key[2]::UUID, 
       short_or_trousers_info[1], short_or_trousers_info[2],
       tshirt_or_shirt_info[1], tshirt_or_shirt_info[2],
       sweater_info[1], sweater_info[2],
       dress_info[1], dress_info[2],
       footwear_info[1], footwear_info[2]
FROM crosstab('
    SELECT ARRAY[clothing.ind::text, h.id::text] AS row_name, clothing.dress_type as cat, ARRAY[clothing.size, clothing.amount]
    FROM houses h
    INNER JOIN (SELECT * FROM (
        SELECT ROW_NUMBER() OVER (PARTITION BY ch.house_id ORDER BY ch.house_id) as ind, ''short_or_trousers'' as dress_type, a.short_or_trousers_size as size, COUNT(*) as amount, ch.house_id as house_id
        FROM children c
        INNER JOIN attires a ON a.id_child = c.id
        INNER JOIN children_houses ch ON ch.child_id = c.id
        WHERE ch.child_id = a.id_child
        GROUP BY ch.house_id, a.short_or_trousers_size

        UNION

        SELECT ROW_NUMBER() OVER (PARTITION BY ch.house_id ORDER BY ch.house_id) as ind, ''tshirt_or_shirt'' as dress_type, a.tshirt_or_shirt_size as size, COUNT(*) as amount, ch.house_id as house_id
        FROM children c
        INNER JOIN attires a ON a.id_child = c.id
        INNER JOIN children_houses ch ON ch.child_id = c.id
        WHERE ch.child_id = a.id_child
        GROUP BY ch.house_id, a.tshirt_or_shirt_size

        UNION

        SELECT ROW_NUMBER() OVER (PARTITION BY ch.house_id ORDER BY ch.house_id) as ind, ''sweater'' as dress_type, a.sweater_size as size, COUNT(*) as amount, ch.house_id as house_id
        FROM children c
        INNER JOIN attires a ON a.id_child = c.id
        INNER JOIN children_houses ch ON ch.child_id = c.id
        WHERE ch.child_id = a.id_child AND a.sweater_size IS NOT NULL
        GROUP BY ch.house_id, a.sweater_size

        UNION

        SELECT ROW_NUMBER() OVER (PARTITION BY ch.house_id ORDER BY ch.house_id) as ind, ''dress'' as dress_type, a.dress_size as size, COUNT(*) as amount, ch.house_id as house_id
        FROM children c
        INNER JOIN attires a ON a.id_child = c.id
        INNER JOIN children_houses ch ON ch.child_id = c.id
        WHERE ch.child_id = a.id_child AND a.dress_size IS NOT NULL
        GROUP BY ch.house_id, a.dress_size

        UNION

        SELECT ROW_NUMBER() OVER (PARTITION BY ch.house_id ORDER BY ch.house_id) as ind, ''footwear'' as dress_type, a.footwear_size as size, COUNT(*) as amount, ch.house_id as house_id
        FROM children c
        INNER JOIN attires a ON a.id_child = c.id
        INNER JOIN children_houses ch ON ch.child_id = c.id
        WHERE ch.child_id = a.id_child
        GROUP BY ch.house_id, a.footwear_size
    ) raw_clothing) clothing ON clothing.house_id = h.id
    ORDER BY h.id
', '
    SELECT *
    FROM (VALUES
        (''short_or_trousers''),
        (''tshirt_or_shirt''),
        (''sweater''),
        (''dress''),
        (''footwear'')
    ) t(dress_type)
') AS (
    key TEXT[],
    short_or_trousers_info bigint[],
    tshirt_or_shirt_info bigint[],
    sweater_info bigint[],
    dress_info bigint[],
    footwear_info bigint[]
) ORDER BY key[2], (CASE COALESCE(short_or_trousers_info[1], 0)
                WHEN 0 THEN 0
                ELSE 1
            END) + 
            (CASE COALESCE(tshirt_or_shirt_info[1], 0)
                WHEN 0 THEN 0
                ELSE 1
            END) + 
            (CASE COALESCE(sweater_info[1], 0)
                WHEN 0 THEN 0
                ELSE 1
            END) + 
            (CASE COALESCE(dress_info[1], 0)
                WHEN 0 THEN 0
                ELSE 1
            END) + 
            (CASE COALESCE(footwear_info[1], 0)
                WHEN 0 THEN 0
                ELSE 1
            END) DESC

/* Delete HOUSE */

SELECT * FROM attires

SELECT * FROM children_houses

DELETE FROM children_houses WHERE house_id = ''2cbe7c2b-d31b-4659-b2f2-80d045255329''