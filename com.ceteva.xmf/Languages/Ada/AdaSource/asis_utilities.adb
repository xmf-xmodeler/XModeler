--------------------------------------------------------------------------------
--  Title:        asis_utilities.adb
--
--  Author:       Armin Descloux
--  Created:      27.12.97
--  Last updated: 25.1.98
--  Tested:
--------------------------------------------------------------------------------



with Ada.Text_IO;             use Ada.Text_IO;
with Ada.Strings;             use Ada.Strings;
with Ada.Strings.Fixed;       use Ada.Strings.Fixed;
with Ada.Characters.Handling; use Ada.Characters.Handling;

with Asis.Compilation_Units;
with Asis.Elements;
with Asis.Declarations;
with Asis.Text;

with Span_Utilities; use Span_Utilities;
with Positions;      use Positions;
with Debugging;      use Debugging;

use Asis;


package body Asis_Utilities is

   function Is_Private_Unit (Unit: in Asis.Declaration) return Boolean is
      U_Kind: Asis.Declaration_Kinds :=
        Asis.Elements.Declaration_Kind(Unit);
      Encl_CU: Asis.Compilation_Unit :=
        Asis.Elements.Enclosing_Compilation_Unit(Unit);
   begin
      return
        (U_Kind = A_Package_Declaration           or else
         U_Kind = A_Procedure_Declaration         or else
         U_Kind = A_Function_Declaration          or else
         U_Kind = A_Generic_Procedure_Declaration or else
         U_Kind = A_Generic_Function_Declaration  or else
         U_Kind = A_Generic_Package_Declaration)          and then
        Asis.Elements.Is_Equal
        (Unit, Asis.Elements.Unit_Declaration(Encl_CU))   and then
        Asis.Compilation_Units.Unit_Class(Encl_CU) = A_Private_Declaration;
   end Is_Private_Unit;

   -----------------------------------------------------------------------------

   function Is_Separate_Unit (Unit: in Asis.Declaration) return Boolean is
      U_Kind: Asis.Declaration_Kinds :=
        Asis.Elements.Declaration_Kind(Unit);
      Encl_CU: Asis.Compilation_Unit :=
        Asis.Elements.Enclosing_Compilation_Unit(Unit);
   begin
      return
        (U_Kind = A_Procedure_Body_Declaration or else
         U_Kind = A_Package_Body_Declaration   or else
         U_Kind = A_Task_Body_Declaration      or else
         U_Kind = A_Protected_Body_Declaration or else
         U_Kind = A_Function_Body_Declaration)          and then
        Asis.Elements.Is_Equal
        (Unit, Asis.Elements.Unit_Declaration(Encl_CU)) and then
        Asis.Compilation_Units.Unit_Class(Encl_CU) = A_Separate_Body;
   end Is_Separate_Unit;

   -----------------------------------------------------------------------------

   function First_Element (List: in Asis.Element_List) return Asis.Element is
   begin
      return List(List'First);
   end First_Element;

   -----------------------------------------------------------------------------

   function Last_Element (List: in Asis.Element_List) return Asis.Element is
   begin
      return List(List'Last);
   end Last_Element;

   -----------------------------------------------------------------------------

   function Is_Here (Element: in Asis.Element) return Boolean is
   begin
      return Asis.Elements.Element_Kind(Element) /= Not_An_Element;
   end Is_Here;

   -----------------------------------------------------------------------------

   function Count (List: in Asis.Element_List) return Natural is
   begin
      return List'Length;
   end Count;



   -----------------------------------------------------------------------------
   -- This is an ad-hoc implementation of the Asis query
   --
   --    Asis.Declarations.Is_Name_Repeated (Declaration: in Asis.Declaration)
   --      return Boolean;
   --
   -- Reason: This query is not yet implemented in Asis-for-GNAT.
   --
   -- Once this will be done, all calls to the function Name_After_End_Repeated
   -- can be replaced by calls to Asis.Declarations.Is_Name_Repeated.
   -----------------------------------------------------------------------------

   function Name_After_End_Repeated (Declaration: in Asis.Declaration)
                                     return Boolean is

      Name: String := To_String(Asis.Declarations.Defining_Name_Image
                                (First_Element(Asis.Declarations.Names
                                               (Declaration))));

      End_Span:       Asis.Text.Span := Get_Span(Declaration, "end", Backward);
      Start_Position: Position       := (Line   => End_Span.Last_Line,
                                         Column => End_Span.Last_Column);
   begin


      case Asis.Elements.Declaration_Kind(Declaration) is
         when A_Package_Declaration |       -- OK
           A_Package_Body_Declaration |     -- OK
           A_Procedure_Body_Declaration |   -- OK
           A_Function_Body_Declaration |    -- OK (also for operator symbols)
           A_Generic_Package_Declaration |  -- OK
           A_Task_Body_Declaration |        -- OK
           A_Protected_Type_Declaration |   -- OK
           A_Single_Protected_Declaration | -- OK
           A_Protected_Body_Declaration |   -- OK
           An_Entry_Body_Declaration =>     -- OK

            --------------------------------------------------------------------
            -- The following is necessary to treat cases as:
            --     package A.B.A.B is
            --     begin
            --        ...
            --     end a.b
            --        .a.
            --     b;
            --------------------------------------------------------------------

            declare
               Name_Repeated: Boolean        := True;
               New_Pos:       Natural        := 0;
               Last_Pos:      Natural        := 0;
               Current_Span:  Asis.Text.Span := Asis.Text.Nil_Span;
            begin

               for I in 1 .. Count(Source  => Name,
                                   Pattern => ".") + 1 loop

                  New_Pos := Index(Source  => Name(Last_Pos + 1 .. Name'Last),
                                   Pattern => ".");

                  if New_Pos = 0 then
                     New_Pos := Name'Last + 1;
                  end if;

                  Current_Span := Get_Span(Declaration,
                                           Name(Last_Pos + 1 .. New_Pos - 1),
                                           Forward,
                                           Start_Position);

                  Name_Repeated :=
                    Name_Repeated and not Asis.Text.Is_Nil(Current_Span);

                  exit when not Name_Repeated;

                  Last_Pos := New_Pos;

                  Start_Position := (Line   => Current_Span.Last_Line,
                                     Column => Current_Span.Last_Column);

                  Increment_Source_Position(Start_Position.Line,
                                            Start_Position.Column,
                                            Declaration);
               end loop;

               return Name_Repeated;

            end;

            --------------------------------------------------------------------

         when A_Task_Type_Declaration => -- OK
            if Is_Here(Asis.Declarations.Type_Declaration_View
                       (Declaration)) then
               return not Asis.Text.Is_Nil(Get_Span(Declaration,
                                                    Name,
                                                    Forward,
                                                    Start_Position));
            else
               return False;
            end if;

            --------------------------------------------------------------------

         when A_Single_Task_Declaration => -- OK
            if Is_Here(Asis.Declarations.Object_Declaration_View
                       (Declaration)) then
               return not Asis.Text.Is_Nil(Get_Span(Declaration,
                                                    Name,
                                                    Forward,
                                                    Start_Position));
            else
               return False;
            end if;

            --------------------------------------------------------------------

         when others =>
            return False;
      end case;

   exception
      when others =>
         Debug_Line
           ("*** " &
            "exception raised in Asis_Utilities.Name_After_End_Repeated ***");
         raise;
   end Name_After_End_Repeated;

end Asis_Utilities;

