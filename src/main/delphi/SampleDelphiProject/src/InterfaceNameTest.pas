unit InterfaceNameTest;

interface

type
    // An interface definition
    IRecyclable = Interface(IInterface)
        // A single function supporting the property
        function GetIsRecyclable : Boolean;
        // A single property
        property isRecyclable : Boolean read GetIsRecyclable;
    end;
implementation

end.