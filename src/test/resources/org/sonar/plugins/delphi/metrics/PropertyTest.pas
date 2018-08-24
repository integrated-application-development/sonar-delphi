unit DemoForm;

interface

type
  TDemo = class 
  published
  	property isFoo : Boolean read foo write foo;
    property isBar : Boolean read bar write bar;
  end;

implementation

begin
end.