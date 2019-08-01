unit Literals;

interface

implementation

procedure MyLiteralsTest;
var
  MyString: String;   // 'STRING_LITERAL'
  MyInteger: Integer; // 'NUMERIC_LITERAL'
  MyDouble: Double;   // 'NUMERIC_LITERAL'
  MyHexNum: Char;     // 'NUMERIC_LITERAL'
begin
  MyString := 'String!';
  MyString := 'Some other string!';
  MyString := 'Wow, there are a lot of strings in here!';
  MyInteger := 1;
  MyInteger := 1234;
  MyDouble := 1234.5;
  MyDouble := 233462344.005;
  MyHexNum := $42;
end;

end.