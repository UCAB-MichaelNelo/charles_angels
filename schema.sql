PGDMP         .                z            charles-angels    13.5 (Debian 13.5-0+deb11u1)    13.5 (Debian 13.5-0+deb11u1)     �           0    0    ENCODING    ENCODING        SET client_encoding = 'UTF8';
                      false            �           0    0 
   STDSTRINGS 
   STDSTRINGS     (   SET standard_conforming_strings = 'on';
                      false            �           0    0 
   SEARCHPATH 
   SEARCHPATH     8   SELECT pg_catalog.set_config('search_path', '', false);
                      false            �           1262    16386    charles-angels    DATABASE     e   CREATE DATABASE "charles-angels" WITH TEMPLATE = template0 ENCODING = 'UTF8' LOCALE = 'en_US.UTF-8';
     DROP DATABASE "charles-angels";
                syfers    false            �            1259    16441    attires    TABLE     �   CREATE TABLE public.attires (
    short_or_trousers_size bigint NOT NULL,
    tshirt_or_shirt_size bigint NOT NULL,
    sweater_size bigint,
    dress_size bigint,
    footwear_size bigint NOT NULL,
    id_child uuid NOT NULL
);
    DROP TABLE public.attires;
       public         heap    charles-angels-admin    false            �            1259    16388    children    TABLE     �   CREATE TABLE public.children (
    id uuid NOT NULL,
    sex character varying(1) NOT NULL,
    photo character varying(255) NOT NULL,
    father_ci bigint,
    mother_ci bigint,
    non_parent_ci bigint
);
    DROP TABLE public.children;
       public         heap    charles-angels-admin    false            �            1259    16436    children_houses    TABLE     `   CREATE TABLE public.children_houses (
    child_id uuid NOT NULL,
    house_id uuid NOT NULL
);
 #   DROP TABLE public.children_houses;
       public         heap    charles-angels-admin    false            �            1259    16426    contacts    TABLE     �   CREATE TABLE public.contacts (
    ci bigint NOT NULL,
    name character varying(50) NOT NULL,
    lastname character varying(75) NOT NULL,
    phone character(12)
);
    DROP TABLE public.contacts;
       public         heap    charles-angels-admin    false            �            1259    16418    houses    TABLE     
  CREATE TABLE public.houses (
    id uuid NOT NULL,
    img character varying(255) NOT NULL,
    name character varying(50) NOT NULL,
    rif bigint NOT NULL,
    phones character(12)[] NOT NULL,
    address character varying(255) NOT NULL,
    max_shares bigint NOT NULL,
    current_shares bigint NOT NULL,
    minimum_age bigint NOT NULL,
    maximum_age bigint NOT NULL,
    current_girls_helped bigint NOT NULL,
    current_boys_helped bigint NOT NULL,
    contact_ci bigint NOT NULL,
    schedule_id uuid NOT NULL
);
    DROP TABLE public.houses;
       public         heap    charles-angels-admin    false            �            1259    16452    people_houses    TABLE     a   CREATE TABLE public.people_houses (
    house_id uuid NOT NULL,
    person_ci bigint NOT NULL
);
 !   DROP TABLE public.people_houses;
       public         heap    charles-angels-admin    false            �            1259    16393    personal_information    TABLE     �   CREATE TABLE public.personal_information (
    ci bigint NOT NULL,
    name character varying(50) NOT NULL,
    lastname character varying(75) NOT NULL,
    birthdate date NOT NULL,
    id_children uuid
);
 (   DROP TABLE public.personal_information;
       public         heap    charles-angels-admin    false            �            1259    16449    related_beneficiaries    TABLE     j   CREATE TABLE public.related_beneficiaries (
    child_id uuid NOT NULL,
    related_ci bigint NOT NULL
);
 )   DROP TABLE public.related_beneficiaries;
       public         heap    charles-angels-admin    false            �            1259    16431 	   schedules    TABLE     �   CREATE TABLE public.schedules (
    id uuid NOT NULL,
    day bigint NOT NULL,
    key bigint NOT NULL,
    start_time time without time zone NOT NULL,
    duration bigint NOT NULL
);
    DROP TABLE public.schedules;
       public         heap    charles-angels-admin    false            E           2606    16392    children children_pkey 
   CONSTRAINT     T   ALTER TABLE ONLY public.children
    ADD CONSTRAINT children_pkey PRIMARY KEY (id);
 @   ALTER TABLE ONLY public.children DROP CONSTRAINT children_pkey;
       public            charles-angels-admin    false    200            K           2606    16430    contacts contacts_pkey 
   CONSTRAINT     T   ALTER TABLE ONLY public.contacts
    ADD CONSTRAINT contacts_pkey PRIMARY KEY (ci);
 @   ALTER TABLE ONLY public.contacts DROP CONSTRAINT contacts_pkey;
       public            charles-angels-admin    false    203            I           2606    16425    houses houses_pkey 
   CONSTRAINT     P   ALTER TABLE ONLY public.houses
    ADD CONSTRAINT houses_pkey PRIMARY KEY (id);
 <   ALTER TABLE ONLY public.houses DROP CONSTRAINT houses_pkey;
       public            charles-angels-admin    false    202            G           2606    16397 .   personal_information personal_information_pkey 
   CONSTRAINT     l   ALTER TABLE ONLY public.personal_information
    ADD CONSTRAINT personal_information_pkey PRIMARY KEY (ci);
 X   ALTER TABLE ONLY public.personal_information DROP CONSTRAINT personal_information_pkey;
       public            charles-angels-admin    false    201            M           2606    16440    schedules schedules_pkey 
   CONSTRAINT     l   ALTER TABLE ONLY public.schedules
    ADD CONSTRAINT schedules_pkey PRIMARY KEY (id, day, key, start_time);
 B   ALTER TABLE ONLY public.schedules DROP CONSTRAINT schedules_pkey;
       public            charles-angels-admin    false    204    204    204    204            R           2606    16444    attires attires_id_child_fkey    FK CONSTRAINT     �   ALTER TABLE ONLY public.attires
    ADD CONSTRAINT attires_id_child_fkey FOREIGN KEY (id_child) REFERENCES public.children(id) ON UPDATE CASCADE ON DELETE CASCADE;
 G   ALTER TABLE ONLY public.attires DROP CONSTRAINT attires_id_child_fkey;
       public          charles-angels-admin    false    2885    200    206            Q           2606    16398    personal_information children    FK CONSTRAINT     �   ALTER TABLE ONLY public.personal_information
    ADD CONSTRAINT children FOREIGN KEY (id_children) REFERENCES public.children(id) ON UPDATE CASCADE ON DELETE CASCADE;
 G   ALTER TABLE ONLY public.personal_information DROP CONSTRAINT children;
       public          charles-angels-admin    false    201    2885    200            N           2606    16403     children children_father_ci_fkey    FK CONSTRAINT     �   ALTER TABLE ONLY public.children
    ADD CONSTRAINT children_father_ci_fkey FOREIGN KEY (father_ci) REFERENCES public.personal_information(ci) ON UPDATE CASCADE ON DELETE CASCADE;
 J   ALTER TABLE ONLY public.children DROP CONSTRAINT children_father_ci_fkey;
       public          charles-angels-admin    false    201    200    2887            O           2606    16408     children children_mother_ci_fkey    FK CONSTRAINT     �   ALTER TABLE ONLY public.children
    ADD CONSTRAINT children_mother_ci_fkey FOREIGN KEY (mother_ci) REFERENCES public.personal_information(ci) ON UPDATE CASCADE ON DELETE CASCADE;
 J   ALTER TABLE ONLY public.children DROP CONSTRAINT children_mother_ci_fkey;
       public          charles-angels-admin    false    2887    200    201            P           2606    16413 $   children children_non_parent_ci_fkey    FK CONSTRAINT     �   ALTER TABLE ONLY public.children
    ADD CONSTRAINT children_non_parent_ci_fkey FOREIGN KEY (non_parent_ci) REFERENCES public.personal_information(ci) ON UPDATE CASCADE ON DELETE CASCADE;
 N   ALTER TABLE ONLY public.children DROP CONSTRAINT children_non_parent_ci_fkey;
       public          charles-angels-admin    false    200    201    2887           